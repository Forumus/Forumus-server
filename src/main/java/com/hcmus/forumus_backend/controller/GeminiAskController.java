package com.hcmus.forumus_backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.forumus_backend.dto.GeminiAskResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hcmus.forumus_backend.service.GeminiService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class GeminiAskController {

    private final GeminiService geminiService;

    public GeminiAskController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/askGemini")
    public GeminiAskResponse askGemini(@RequestBody String question) {
        // Placeholder response for demonstration purposes
        String geminiResponse = geminiService.askGemini(question);
        return new GeminiAskResponse(geminiResponse);
    }
    
    
}
