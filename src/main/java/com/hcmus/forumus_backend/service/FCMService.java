package com.hcmus.forumus_backend.service;

import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending Firebase Cloud Messaging notifications
 * 
 * This service handles sending push notifications to Android clients
 * when new chat messages are received.
 */
@Service
public class FCMService {
    
    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);
    
    /**
     * Send a chat message notification to a specific device
     * 
     * @param fcmToken The recipient's FCM token
     * @param senderName The sender's full name (will be notification title)
     * @param messageContent The message content (will be notification body)
     * @param chatId The chat ID
     * @param senderId The sender's user ID
     * @param senderEmail The sender's email
     * @param senderPictureUrl The sender's profile picture URL
     * @return true if sent successfully, false otherwise
     */
    public boolean sendChatNotification(
        String fcmToken,
        String senderName,
        String messageContent,
        String chatId,
        String senderId,
        String senderEmail,
        String senderPictureUrl
    ) {
        try {
            // Truncate long messages
            String truncatedContent = messageContent;
            if (messageContent.length() > 100) {
                truncatedContent = messageContent.substring(0, 100) + "...";
            }
            
            // Create notification
            Notification notification = Notification.builder()
                .setTitle(senderName)
                .setBody(truncatedContent)
                .build();
            
            // Create data payload
            Map<String, String> data = new HashMap<>();
            data.put("chatId", chatId);
            data.put("senderId", senderId);
            data.put("senderName", senderName);
            data.put("senderEmail", senderEmail != null ? senderEmail : "");
            data.put("senderPictureUrl", senderPictureUrl != null ? senderPictureUrl : "");
            data.put("messageContent", truncatedContent);
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            // Build the message
            Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .setChannelId("chat_notifications")
                        .build())
                    .build())
                .build();
            
            // Send the message
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Successfully sent notification: {}", response);
            return true;
            
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send notification to token: {}", fcmToken, e);
            
            // Handle invalid token
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                logger.warn("Invalid or unregistered token: {}", fcmToken);
                // You should remove this token from your database
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending notification", e);
            return false;
        }
    }
    
    /**
     * Send notification for image message
     */
    public boolean sendImageMessageNotification(
        String fcmToken,
        String senderName,
        String messageText,
        int imageCount,
        String chatId,
        String senderId,
        String senderEmail,
        String senderPictureUrl
    ) {
        String notificationBody;
        if (messageText != null && !messageText.isEmpty()) {
            notificationBody = messageText + " 📷";
        } else {
            notificationBody = "Sent " + imageCount + " photo" + (imageCount > 1 ? "s" : "");
        }
        
        return sendChatNotification(
            fcmToken,
            senderName,
            notificationBody,
            chatId,
            senderId,
            senderEmail,
            senderPictureUrl
        );
    }
    
    /**
     * Send notification to multiple devices (for group chats)
     */
    public void sendMulticastNotification(
        java.util.List<String> fcmTokens,
        String senderName,
        String messageContent,
        String chatId,
        String senderId
    ) {
        try {
            // Truncate long messages
            String truncatedContent = messageContent;
            if (messageContent.length() > 100) {
                truncatedContent = messageContent.substring(0, 100) + "...";
            }
            
            // Create notification
            Notification notification = Notification.builder()
                .setTitle(senderName)
                .setBody(truncatedContent)
                .build();
            
            // Create data payload
            Map<String, String> data = new HashMap<>();
            data.put("chatId", chatId);
            data.put("senderId", senderId);
            data.put("senderName", senderName);
            data.put("messageContent", truncatedContent);
            
            // Build multicast message
            MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(fcmTokens)
                .setNotification(notification)
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build())
                .build();
            
            // Send to multiple devices
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            logger.info("Successfully sent {} notifications, {} failures",
                response.getSuccessCount(), response.getFailureCount());
            
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send multicast notification", e);
        }
    }
}
