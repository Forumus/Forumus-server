package com.hcmus.forumus_backend.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.forumus_backend.dto.GeminiAskResponse;
import com.hcmus.forumus_backend.dto.post.PostIdRequest;
import com.hcmus.forumus_backend.dto.post.PostDTO;
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

    public PostController(PostService PostService) {
        this.PostService = PostService;
    }

    @PostMapping("/askGemini")
    public GeminiAskResponse askGemini(@RequestBody String question) {
        String geminiResponse = PostService.askGemini(question);
        return new GeminiAskResponse(geminiResponse);
    }

    @PostMapping("/validatePost")
    public PostValidationResponse validatePost(@RequestBody PostIdRequest request) {
        try {
            PostDTO post = PostService.getPostById(request.getPostId());
            if (post == null) {
                return new PostValidationResponse(false, "Post not found");
            }
            PostValidationResponse validationResponse = PostService.validatePost(post.getTitle(), post.getContent());

            PostService.updatePostStatus(request.getPostId(), validationResponse.isValid() ? "APPROVED" : "REJECTED");
            return validationResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return new PostValidationResponse(false, "Error fetching post: " + e.getMessage());
        }
    }

    @PostMapping("/getSuggestedTopics")
    public Map<String, Object> extractTopics(@RequestBody PostValidationRequest request) {
        return PostService.extractTopics(request.getTitle(), request.getContent());
    }
}
