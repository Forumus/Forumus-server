package com.hcmus.forumus_backend.service;

import org.springframework.stereotype.Service;

import com.google.genai.types.Part;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

@Service
public class GeminiService {
    private final Client geminiClient;
    private final GenerateContentConfig generateContentConfig;
    private final String GEMINI_MODEL_NAME = "gemini-2.5-flash";

    public GeminiService(Client geminiClient, GenerateContentConfig generateContentConfig) {
        this.geminiClient = geminiClient;
        this.generateContentConfig = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText("You are a helpful assistant.")))
                .build();
    }

    public String askGemini(String prompt) {
        GenerateContentResponse response = geminiClient.models.generateContent(
                GEMINI_MODEL_NAME,
                prompt,
                generateContentConfig);

        String responseJson = response.toJson();
        System.out.println("Gemini Response: " + responseJson);

        // Extract text content from the response
        if (response.candidates().isPresent() && !response.candidates().get().isEmpty()) {
            var candidate = response.candidates().get().get(0);
            if (candidate.content().isPresent() && candidate.content().get().parts().isPresent()) {
                var parts = candidate.content().get().parts().get();
                if (!parts.isEmpty() && parts.get(0).text().isPresent()) {
                    return parts.get(0).text().get();
                }
            }
        }

        return responseJson;
    }
}
