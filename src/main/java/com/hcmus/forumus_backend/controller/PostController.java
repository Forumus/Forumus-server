package com.hcmus.forumus_backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.forumus_backend.dto.GeminiAskResponse;
import com.hcmus.forumus_backend.dto.post.PostValidationRequest;
import com.hcmus.forumus_backend.dto.post.PostValidationResponse;
import com.hcmus.forumus_backend.dto.topic.TopicResponse;

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
    public PostValidationResponse validatePost(@RequestBody PostValidationRequest request) {
        return PostService.validatePost(request.getTitle(), request.getContent());
    }

    @PostMapping("/extractTopics")
    public List<TopicResponse> extractTopics(@RequestBody PostValidationRequest request) {
        return PostService.extractTopics(request.getTitle(), request.getContent());
    }
}
