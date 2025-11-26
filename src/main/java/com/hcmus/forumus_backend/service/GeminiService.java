package com.hcmus.forumus_backend.service;

import org.springframework.stereotype.Service;

import com.google.genai.types.Part;
import com.hcmus.forumus_backend.dto.post.PostValidationResponse;
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
                .systemInstruction(Content.fromParts(Part.fromText("You are a helpful assistant for an academic forum. You should help ensure that posts adhere to community guidelines suitable for university students.")))
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

    public PostValidationResponse validatePost(String title, String content) {
        String prompt = """
                Please validate the following post for adherence to community guidelines.
                The post should not contain any offensive language, hate speech, personal attacks, or inappropriate content.
                The post should be suitable for university students and respect diversity and inclusion. 
                The content should be relevant to academic topics and promote a positive learning environment.
                Respond with this format:
                Valid: <true/false>
                Reasons: <list of reasons if invalid, else leave blank>

                Here is the post:
                Title: "%s"
                Content: "%s"
                """.formatted(title, content);

        String geminiResponse = askGemini(prompt);

        boolean isValid = false;
        String reasons = "";

        String[] lines = geminiResponse.split("\n");
        for (String line : lines) {
            if (line.startsWith("Valid:")) {
                String validValue = line.substring(6).trim().toLowerCase();
                isValid = validValue.equals("true");
            } else if (line.startsWith("Reasons:")) {
                reasons = line.substring(8).trim();
            }
        }

        return new PostValidationResponse(isValid, reasons);
    }

    
}
