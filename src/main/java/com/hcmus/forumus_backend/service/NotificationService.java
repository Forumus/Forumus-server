package com.hcmus.forumus_backend.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.hcmus.forumus_backend.dto.notification.NotificationTriggerRequest;
import com.hcmus.forumus_backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final Firestore db;
    private final UserService userService;
    private final FCMService fcmService;

    public NotificationService(Firestore db, UserService userService, FCMService fcmService) {
        this.db = db;
        this.userService = userService;
        this.fcmService = fcmService;
    }

    public boolean triggerNotification(NotificationTriggerRequest request) {
        try {
            // 1. Validate inputs
            if (request.getTargetUserId() == null || request.getTargetUserId().isEmpty()) {
                logger.warn("Notification validation failed: targetUserId is missing");
                return false;
            }

            // Don't notify if actor is the same as target user (self-action)
            if (request.getTargetUserId().equals(request.getActorId())) {
                logger.info("Skipping notification: Actor is target user");
                return true; // Use true because it's not an error, just logic
            }

            // 2. Fetch target user to get FCM token
            User targetUser;
            try {
                targetUser = userService.getUserById(request.getTargetUserId());
            } catch (Exception e) {
                logger.error("Failed to fetch target user: {}", request.getTargetUserId(), e);
                return false;
            }

            // 3. Prepare Notification Data for Firestore
            String notificationId = UUID.randomUUID().toString();
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("id", notificationId);
            notificationData.put("type", request.getType());
            notificationData.put("actorId", request.getActorId());
            notificationData.put("actorName", request.getActorName());
            notificationData.put("targetId", request.getTargetId());
            notificationData.put("previewText", request.getPreviewText());
            notificationData.put("createdAt", Timestamp.now());
            notificationData.put("isRead", false);

            // 4. Write to Firestore: users/{targetUserId}/notifications/{notificationId}
            db.collection("users")
                .document(request.getTargetUserId())
                .collection("notifications")
                .document(notificationId)
                .set(notificationData);

            logger.info("Notification saved to Firestore: {}", notificationId);

            // 5. Send FCM Push Notification
            if (targetUser.getFcmToken() != null && !targetUser.getFcmToken().isEmpty()) {
                String title = generateNotificationTitle(request);
                String body = generateNotificationBody(request);

                Map<String, String> data = new HashMap<>();
                data.put("type", "general_notification");
                data.put("notificationId", notificationId);
                data.put("targetId", request.getTargetId()); // Post/Comment ID for deep link
                data.put("click_action", "FLUTTER_NOTIFICATION_CLICK"); // Standard, but we handle intent in Android

                fcmService.sendGeneralNotification(targetUser.getFcmToken(), title, body, data);
            } else {
                logger.info("Target user has no FCM token, skipping push notification");
            }

            return true;

        } catch (Exception e) {
            logger.error("Error triggering notification", e);
            return false;
        }
    }

    private String generateNotificationTitle(NotificationTriggerRequest request) {
        return switch (request.getType()) {
            case "UPVOTE" -> "New Upvote";
            case "COMMENT" -> "New Comment";
            case "REPLY" -> "New Reply";
            default -> "New Notification";
        };
    }

    private String generateNotificationBody(NotificationTriggerRequest request) {
        String actor = request.getActorName() != null ? request.getActorName() : "Someone";
        String preview = request.getPreviewText() != null ? request.getPreviewText() : "";
        if (preview.length() > 50) preview = preview.substring(0, 50) + "...";

        return switch (request.getType()) {
            case "UPVOTE" -> actor + " upvoted your post: " + preview;
            case "COMMENT" -> actor + " commented on your post: " + preview;
            case "REPLY" -> actor + " replied to your comment: " + preview;
            default -> actor + " interacted with your content.";
        };
    }
}
