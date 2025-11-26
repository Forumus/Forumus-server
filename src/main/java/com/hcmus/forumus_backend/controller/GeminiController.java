package com.hcmus.forumus_backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.forumus_backend.dto.GeminiAskResponse;
import com.hcmus.forumus_backend.dto.post.PostValidationRequest;
import com.hcmus.forumus_backend.dto.post.PostValidationResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hcmus.forumus_backend.service.GeminiService;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/askGemini")
    public GeminiAskResponse askGemini(@RequestBody String question) {
        String geminiResponse = geminiService.askGemini(question);
        return new GeminiAskResponse(geminiResponse);
    }

    @PostMapping("/validatePost")
    public PostValidationResponse validatePost(@RequestBody PostValidationRequest request) {
        return geminiService.validatePost(request.getTitle(), request.getContent());
    }
}
