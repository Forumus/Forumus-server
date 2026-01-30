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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@Component
public class FirestoreMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreMessageListener.class);

    @Autowired
    private Firestore firestore;

    @Autowired(required = false)
    private Optional<FCMService> fcmService = Optional.empty();

    @Autowired(required = false)
    private Optional<UserService> userService = Optional.empty();

    private ListenerRegistration listenerRegistration;
    private final Set<String> processedMessages = ConcurrentHashMap.newKeySet();
    private boolean isInitialSnapshot = true;

    /**
     * Start listening to Firestore when Spring Boot application starts
     */
    @PostConstruct
    public void startListening() {
        try {
            logger.info("Starting Firestore message listener...");

            if (!fcmService.isPresent() || !userService.isPresent()) {
                logger.warn("FCMService or UserService not found. Listener will not send notifications.");
            }

            // Listen to all message creations using a collection group query
            listenerRegistration = firestore.collectionGroup("messages")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        logger.error("Error listening to messages", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        // Skip initial snapshot - only process changes after listener is attached
                        if (isInitialSnapshot) {
                            logger.info("Skipping initial snapshot with {} documents", querySnapshot.size());
                            // Pre-populate processedMessages to skip old messages
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                processedMessages.add(doc.getId());
                            }
                            isInitialSnapshot = false;
                            return;
                        }

                        for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                String messageId = change.getDocument().getId();

                                // Prevent duplicate processing
                                if (!processedMessages.add(messageId)) {
                                    logger.debug("Message {} already processed, skipping", messageId);
                                    continue;
                                }

                                // New message added
                                handleNewMessage(change.getDocument());
                            }
                        }
                    }
                });

            logger.info("Firestore listener started successfully");
        } catch (Exception e) {
            logger.error("Failed to start Firestore listener", e);
        }
    }

    private void handleNewMessage(DocumentSnapshot messageDoc) {
        try {
            if (!fcmService.isPresent() || !userService.isPresent()) {
                logger.debug("Skipping notification: FCMService or UserService not available");
                return;
            }

            Map<String, Object> message = messageDoc.getData();
            if (message == null)
                return;

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
            User sender = userService.get().getUserById(senderId);
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
            User recipient = userService.get().getUserById(recipientId);
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

                success = fcmService.get().sendImageMessageNotification(
                        recipient.getFcmToken(),
                        sender.getFullName(),
                        content,
                        imageCount,
                        chatId,
                        senderId,
                        sender.getEmail(),
                        sender.getProfilePictureUrl());
            } else {
                success = fcmService.get().sendChatNotification(
                        recipient.getFcmToken(),
                        sender.getFullName(),
                        content != null ? content : "",
                        chatId,
                        senderId,
                        sender.getEmail(),
                        sender.getProfilePictureUrl());
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

    @PreDestroy
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            logger.info("Firestore listener stopped");
        }
    }
}
