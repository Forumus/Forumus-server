package com.hcmus.forumus_backend.listener;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.DocumentSnapshot;

import com.hcmus.forumus_backend.service.FCMService;
import com.hcmus.forumus_backend.service.UserService;
import com.hcmus.forumus_backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Alternative approach: Listen to Firestore changes in Spring Boot
 * 
 * This is similar to Cloud Functions but runs in your Spring Boot server.
 * Whenever a new message is added to Firestore, this listener is triggered
 * and sends an FCM notification.
 * 
 * Benefits:
 * - Keep using Firestore as primary database
 * - No need to change Android app's ChatRepository
 * - Spring Boot acts as a listener service
 * - Can add complex business logic here
 */
@Component
public class FirestoreMessageListener {
    
    private static final Logger logger = LoggerFactory.getLogger(FirestoreMessageListener.class);
    
    @Autowired
    private Firestore firestore;
    
    @Autowired
    private FCMService fcmService;
    
    @Autowired
    private UserService userService;
    
    private ListenerRegistration listenerRegistration;
    
    /**
     * Start listening to Firestore when Spring Boot application starts
     */
    @PostConstruct
    public void startListening() {
        logger.info("Starting Firestore message listener...");
        
        // Listen to all message creations using a collection group query
        listenerRegistration = firestore.collectionGroup("messages")
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    logger.error("Error listening to messages", error);
                    return;
                }
                
                if (querySnapshot != null) {
                    for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            // New message added
                            handleNewMessage(change.getDocument());
                        }
                    }
                }
            });
        
        logger.info("Firestore listener started successfully");
    }
    
    /**
     * Handle new message and send notification
     */
    private void handleNewMessage(DocumentSnapshot messageDoc) {
        try {
            Map<String, Object> message = messageDoc.getData();
            if (message == null) return;
            
            String messageType = (String) message.get("type");
            
            // Don't send notification for deleted messages
            if ("DELETED".equals(messageType)) {
                logger.debug("Skipping notification for deleted message");
                return;
            }
            
            String senderId = (String) message.get("senderId");
            String content = (String) message.get("content");
            
            // Get chat ID from document path (chats/{chatId}/messages/{messageId})
            String chatId = messageDoc.getReference().getParent().getParent().getId();
            
            logger.info("New message in chat {}: {}", chatId, messageDoc.getId());
            
            // Get sender info
            User sender = userService.getUserById(senderId);
            if (sender == null) {
                logger.warn("Sender not found: {}", senderId);
                return;
            }
            
            // Get chat to find recipient
            DocumentSnapshot chatDoc = firestore.collection("chats")
                .document(chatId)
                .get()
                .get();
            
            if (!chatDoc.exists()) {
                logger.warn("Chat not found: {}", chatId);
                return;
            }
            
            @SuppressWarnings("unchecked")
            List<String> userIds = (List<String>) chatDoc.get("userIds");
            if (userIds == null || userIds.size() < 2) {
                logger.warn("Invalid userIds in chat: {}", chatId);
                return;
            }
            
            // Find recipient (not the sender)
            String recipientId = userIds.stream()
                .filter(id -> !id.equals(senderId))
                .findFirst()
                .orElse(null);
            
            if (recipientId == null) {
                logger.warn("No recipient found in chat: {}", chatId);
                return;
            }
            
            // Get recipient info and FCM token
            User recipient = userService.getUserById(recipientId);
            if (recipient == null || recipient.getFcmToken() == null) {
                logger.info("Recipient {} has no FCM token", recipientId);
                return;
            }
            
            // Send notification
            boolean success;
            if ("IMAGE".equals(messageType)) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) message.get("imageUrls");
                int imageCount = imageUrls != null ? imageUrls.size() : 1;
                
                success = fcmService.sendImageMessageNotification(
                    recipient.getFcmToken(),
                    sender.getFullName(),
                    content,
                    imageCount,
                    chatId,
                    senderId,
                    sender.getEmail(),
                    sender.getProfilePictureUrl()
                );
            } else {
                success = fcmService.sendChatNotification(
                    recipient.getFcmToken(),
                    sender.getFullName(),
                    content != null ? content : "",
                    chatId,
                    senderId,
                    sender.getEmail(),
                    sender.getProfilePictureUrl()
                );
            }
            
            if (success) {
                logger.info("Notification sent to {} for message in chat {}", recipientId, chatId);
            } else {
                logger.warn("Failed to send notification to {}", recipientId);
            }
            
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error handling new message", e);
        } catch (Exception e) {
            logger.error("Unexpected error in message listener", e);
        }
    }
    
    /**
     * Stop listening when Spring Boot application shuts down
     */
    @PreDestroy
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            logger.info("Firestore listener stopped");
        }
    }
}
