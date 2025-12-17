package com.hcmus.forumus_backend.service;

import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.genai.types.Part;
import com.hcmus.forumus_backend.dto.post.PostDTO;
import com.hcmus.forumus_backend.dto.post.PostValidationResponse;
import com.hcmus.forumus_backend.dto.topic.TopicResponse;
import com.hcmus.forumus_backend.enums.PostStatus;
import com.hcmus.forumus_backend.listener.TopicsListener;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class PostService {
    private final Client geminiClient;
    private final GenerateContentConfig generateContentConfig;
    private final String GEMINI_MODEL_NAME = "gemini-2.5-flash";
    private final String instructionPrompt = """
            You are a helpful assistant for an academic forum.
            You should help ensure that posts adhere to community guidelines suitable for university students.
            You should answer in the language of the user's prompt.
            """;

    private final TopicService topicService;
    private final TopicsListener topicsListener;
    private final Firestore db;
    private final ObjectMapper objectMapper;

    public PostService(Client geminiClient, GenerateContentConfig generateContentConfig, TopicService topicService,
            TopicsListener topicsListener, Firestore db) {
        this.geminiClient = geminiClient;
        this.generateContentConfig = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(instructionPrompt)))
                .build();
        this.topicService = topicService;
        this.topicsListener = topicsListener;
        this.db = db;
        this.objectMapper = new ObjectMapper();
    }

    public PostDTO getPostById(String postId) throws ExecutionException, InterruptedException {
        if (postId == null) {
            return null;
        }
        ApiFuture<DocumentSnapshot> future = this.db
                .collection("posts")
                .document(postId)
                .get();
        DocumentSnapshot document = future.get();

        if (document.exists() && PostStatus.PENDING.getValue().equals(document.getString("status"))) {
            PostDTO post = new PostDTO(
                    postId,
                    document.getString("title"),
                    document.getString("content"));
            return post;
        }
        return null;
    }

    public Boolean updatePostStatus(String postId, String status) throws ExecutionException, InterruptedException {
        if (postId == null || status == null) {
            return false;
        }
        ApiFuture<DocumentSnapshot> future = this.db
                .collection("posts")
                .document(postId)
                .get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            this.db.collection("posts")
                    .document(postId)
                    .update("status", status);
            return true;
        }
        return false;
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
                Respond with a JSON object in this format: {"valid": true/false, "reasons": "list of reasons if invalid, else empty string"}

                Here is the post:
                Title: "%s"
                Content: "%s"
                """
                .formatted(title, content);

        String geminiResponse = askGemini(prompt);

        boolean isValid = false;
        String reasons = "";

        try {
            // Extract JSON from response (in case there's additional text)
            String jsonString = geminiResponse;
            int jsonStart = geminiResponse.indexOf('{');
            int jsonEnd = geminiResponse.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                jsonString = geminiResponse.substring(jsonStart, jsonEnd + 1);
            }

            JsonNode jsonNode = objectMapper.readTree(jsonString);
            isValid = jsonNode.get("valid").asBoolean();
            reasons = jsonNode.get("reasons").asText();
        } catch (Exception e) {
            e.printStackTrace();
            reasons = "Error parsing response: " + e.getMessage();
        }

        return new PostValidationResponse(isValid, reasons);
    }

    public Map<String, Object> extractTopics(String title, String content) {
        List<TopicResponse> topicResponses = topicsListener.getAllTopics();

        String topics = topicResponses.stream()
                .map(TopicResponse::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String prompt = """
                Please extract at most 3 main topics from the following post. The topics must be chosen from this list: %s.
                Respond with a JSON object in this format: {"topics": ["topic_name_1", "topic_name_2", "topic_name_3"]}

                Here is the post:
                Title: "%s"
                Content: "%s"
                """
                .formatted(topics, title, content);

        String geminiResponse = askGemini(prompt);

        final String[] topicsArray;
        try {
            // Extract JSON from response (in case there's additional text)
            String jsonString = geminiResponse;
            int jsonStart = geminiResponse.indexOf('{');
            int jsonEnd = geminiResponse.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                jsonString = geminiResponse.substring(jsonStart, jsonEnd + 1);
            }

            JsonNode jsonNode = objectMapper.readTree(jsonString);
            List<String> topicsList = objectMapper.convertValue(
                    jsonNode.get("topics"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            topicsArray = topicsList.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "topics", List.of());
        }

        return Map.of(
                "success", true,
                "topics", topicResponses.stream()
                        .filter(topic -> {
                            for (String t : topicsArray) {
                                if (topic.getName().equalsIgnoreCase(t)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .limit(3)
                        .toList());
    }
}
