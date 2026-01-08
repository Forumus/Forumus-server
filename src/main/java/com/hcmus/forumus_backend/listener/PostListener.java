package com.hcmus.forumus_backend.listener;

import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.hcmus.forumus_backend.dto.post.PostValidationResponse;
import com.hcmus.forumus_backend.enums.PostStatus;
import com.hcmus.forumus_backend.service.PostService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class PostListener {

    private static final Logger logger = LoggerFactory.getLogger(TopicsListener.class);

    @Autowired
    private Firestore firestore;

    @Autowired
    private PostService postService;
    
    @Autowired
    private com.hcmus.forumus_backend.service.NotificationService notificationService;

    private ListenerRegistration listenerRegistration;
    private boolean isInitialSnapshot = true;

    @PostConstruct
    public void startListening() {
        try {
            logger.info("Starting post listener...");

            listenerRegistration = firestore.collection("posts")
                    .addSnapshotListener((querySnapshot, error) -> {
                        if (error != null) {
                            logger.error("Error listening to posts", error);
                            return;
                        }

                        if (querySnapshot != null) {
                            if (isInitialSnapshot) {
                                isInitialSnapshot = false;
                                return; // Skip initial snapshot
                            }

                            for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case ADDED:
                                        handleNewPost(dc.getDocument());
                                        break;
                                    case MODIFIED:
                                        logger.info("Post modified: {}", dc.getDocument().getId());
                                        break;
                                    case REMOVED:
                                        logger.info("Post removed: {}", dc.getDocument().getId());
                                        break;
                                }
                            }
                        }
                    });

            logger.info("Post listener started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start post listener", e);
        }
    }

    private void handleNewPost(QueryDocumentSnapshot document) {
        try {
            String postId = document.getId();
            String title = document.getString("title");
            String content = document.getString("content");
            String status = document.getString("status");
            String authorId = document.getString("authorId");

            logger.info("New post added: {} with status: {}", postId, status);

            // Only validate posts with PENDING status
            if (!PostStatus.PENDING.getValue().equals(status)) {
                logger.debug("Skipping validation for post {} - status is not PENDING", postId);
                return;
            }

            if (title == null || content == null) {
                logger.warn("Post {} has null title or content, skipping validation", postId);
                return;
            }

            // Validate the post
            logger.info("Validating post: {}", postId);
            PostValidationResponse validationResponse = postService.validatePost(title, content);

            // Update post status based on validation result
            String newStatus = validationResponse.isValid() ? "APPROVED" : "REJECTED";
            postService.updatePostStatus(postId, newStatus);

            logger.info("Post {} validation complete - Status: {}, Reasons: {}",
                    postId, newStatus, validationResponse.getMessage());
            
            if ("REJECTED".equals(newStatus)) {
                logger.info("Post rejected by Listener. Triggering notification for author: {}", authorId);
                com.hcmus.forumus_backend.dto.notification.NotificationTriggerRequest notificationRequest = 
                    new com.hcmus.forumus_backend.dto.notification.NotificationTriggerRequest();
                
                notificationRequest.setType("POST_REJECTED");
                notificationRequest.setTargetUserId(authorId);
                notificationRequest.setTargetId(postId);
                notificationRequest.setOriginalPostTitle(title);
                notificationRequest.setOriginalPostContent(content);
                notificationRequest.setPreviewText(title); 
                notificationRequest.setRejectionReason(validationResponse.getMessage());
                notificationRequest.setActorName("Verification System");
                notificationRequest.setActorId("system_ai");

                notificationService.triggerNotification(notificationRequest);
            }

        } catch (Exception e) {
            logger.error("Error handling new post", e);
        }
    }

    @PreDestroy
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            logger.info("Post listener stopped.");
        }
    }

}
