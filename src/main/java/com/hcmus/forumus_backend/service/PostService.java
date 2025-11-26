package com.hcmus.forumus_backend.service;

import org.springframework.stereotype.Service;

import com.google.genai.types.Part;
import com.hcmus.forumus_backend.dto.post.PostValidationResponse;
import com.hcmus.forumus_backend.dto.topic.TopicResponse;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.hcmus.forumus_backend.service.TopicService;
import java.util.List;

@Service
public class PostService {
    private final Client geminiClient;
    private final GenerateContentConfig generateContentConfig;
    private final String GEMINI_MODEL_NAME = "gemini-2.5-flash";

    private final TopicService topicService;

    public PostService(Client geminiClient, GenerateContentConfig generateContentConfig, TopicService topicService) {
        this.geminiClient = geminiClient;
        this.generateContentConfig = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText("You are a helpful assistant for an academic forum. You should help ensure that posts adhere to community guidelines suitable for university students.")))
                .build();
        this.topicService = topicService;
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

    public List<TopicResponse> extractTopics(String title, String content) {
        List<TopicResponse> topicResponses;
        try {
            topicResponses = topicService.getAllTopics();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Return empty list on error
        }

        String topics = topicResponses.stream()
                .map(TopicResponse::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String prompt = """
                Please extract at most 3 main topics from the following post. The topics must be chosen from this list: %s.
                Respond with a comma-separated list of topics.

                Here is the post:
                Title: "%s"
                Content: "%s"
                """.formatted(topics, title, content);

        String geminiResponse = askGemini(prompt);

        String[] topicsArray = geminiResponse.split(",");
        for (int i = 0; i < topicsArray.length; i++) {
            topicsArray[i] = topicsArray[i].trim();
        }

        return topicResponses.stream()
                .filter(topic -> {
                    for (String t : topicsArray) {
                        if (topic.getName().equalsIgnoreCase(t)) {
                            return true;
                        }
                    }
                    return false;
                })
                .limit(3)
                .toList();
    }
}
