package com.hcmus.forumus_backend.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.forumus_backend.dto.GeminiAskResponse;
import com.hcmus.forumus_backend.dto.post.PostIdRequest;
import com.hcmus.forumus_backend.dto.post.PostDTO;
import com.hcmus.forumus_backend.dto.post.PostSummaryRequest;
import com.hcmus.forumus_backend.dto.post.PostSummaryResponse;
import com.hcmus.forumus_backend.dto.post.PostValidationRequest;
import com.hcmus.forumus_backend.dto.post.PostValidationResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hcmus.forumus_backend.service.PostService;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class PostController {

    private final PostService PostService;
    private final com.hcmus.forumus_backend.service.NotificationService notificationService;

    public PostController(PostService PostService, com.hcmus.forumus_backend.service.NotificationService notificationService) {
        this.PostService = PostService;
        this.notificationService = notificationService;
    }

    @PostMapping("/askGemini")
    public GeminiAskResponse askGemini(@RequestBody String question) {
        String geminiResponse = PostService.askGemini(question);
        return new GeminiAskResponse(geminiResponse);
    }

    @PostMapping("/validatePost")
    public PostValidationResponse validatePost(@RequestBody PostIdRequest request) {
        try {
            System.out.println("Validating Post ID: " + request.getPostId());
            PostDTO post = PostService.getPostById(request.getPostId());

            if (post == null) {
                System.out.println("Post not found for ID: " + request.getPostId());
                return new PostValidationResponse(false, "Post not found");
            }
            System.out.println("Post found. Title: " + post.getTitle() + ", AuthorID: " + post.getAuthorId());
            
            PostValidationResponse validationResponse = PostService.validatePost(post.getTitle(), post.getContent());
            System.out.println("Validation Result: " + validationResponse.isValid());

            if (validationResponse.isValid()) {
                PostService.updatePostStatus(request.getPostId(), "APPROVED");
            } else {
                PostService.updatePostStatus(request.getPostId(), "REJECTED");
                System.out.println("Post Rejected. Triggering notification for Author: " + post.getAuthorId());
                
                // Trigger rejection notification
                com.hcmus.forumus_backend.dto.notification.NotificationTriggerRequest notificationRequest = 
                    new com.hcmus.forumus_backend.dto.notification.NotificationTriggerRequest();
                
                notificationRequest.setType("POST_REJECTED");
                notificationRequest.setTargetUserId(post.getAuthorId());
                notificationRequest.setTargetId(post.getPostId());
                notificationRequest.setOriginalPostTitle(post.getTitle());
                notificationRequest.setOriginalPostContent(post.getContent());
                notificationRequest.setPreviewText(post.getTitle()); 
                notificationRequest.setRejectionReason(validationResponse.getMessage());
                notificationRequest.setActorName("Verification System");
                notificationRequest.setActorId("system_ai");

                try {
                    notificationService.triggerNotification(notificationRequest);
                    System.out.println("Notification trigger called successfully.");
                } catch (Exception e) {
                    System.err.println("Error triggering notification: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return validationResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return new PostValidationResponse(false, "Error fetching post: " + e.getMessage());
        }
    }

    @PostMapping("/summarize")
    public PostSummaryResponse summarizePost(@RequestBody PostSummaryRequest request) {
        System.out.println("Summarizing Post ID: " + request.getPostId());
        return PostService.summarizePost(request.getPostId());
    }
    
    @PostMapping("/getSuggestedTopics")
    public Map<String, Object> extractTopics(@RequestBody PostValidationRequest request) {
        return PostService.extractTopics(request.getTitle(), request.getContent());
    }
}
