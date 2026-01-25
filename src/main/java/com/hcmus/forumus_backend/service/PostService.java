package com.hcmus.forumus_backend.service;

import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.genai.types.Part;
import com.hcmus.forumus_backend.dto.post.PostDTO;
import com.hcmus.forumus_backend.dto.post.PostSummaryResponse;
import com.hcmus.forumus_backend.dto.post.PostValidationResponse;
import com.hcmus.forumus_backend.dto.topic.TopicResponse;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    private final TopicsListener topicsListener;
    private final Firestore db;
    private final ObjectMapper objectMapper;

    public PostService(Client geminiClient, GenerateContentConfig generateContentConfig, TopicService topicService,
            TopicsListener topicsListener, Firestore db) {
        this.geminiClient = geminiClient;
        this.generateContentConfig = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(instructionPrompt)))
                .build();
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

        if (document.exists()) {
            PostDTO post = new PostDTO(
                    postId,
                    document.getString("title"),
                    document.getString("content"),
                    document.getString("authorId"));
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
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
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
            });

            return future.get(60, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.err.println("Gemini request timed out after 60 seconds");
            throw new RuntimeException("AI response timeout - request exceeded 60 seconds", e);
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            throw new RuntimeException("Error calling AI service: " + e.getMessage(), e);
        }
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

    /**
     * Generates an AI-powered summary for a post.
     * 
     * @param postId The ID of the post to summarize
     * @return PostSummaryResponse containing the summary or error message
     */
    public PostSummaryResponse summarizePost(String postId) {
        System.out.println("Generating summary for Post ID: " + postId);
        
        try {
            // Fetch post from Firestore
            PostDTO post = getPostById(postId);
            
            if (post == null) {
                System.out.println("Post not found for ID: " + postId);
                return PostSummaryResponse.error("Post not found");
            }
            
            String title = post.getTitle();
            String content = post.getContent();
            
            // Truncate content if too long (max 5000 chars to avoid token limits)
            if (content != null && content.length() > 5000) {
                content = content.substring(0, 5000) + "...";
            }
            
            System.out.println("Summarizing post - Title: " + title);
            
            // Build the summarization prompt
            String prompt = """
                Please provide a concise summary (2-3 sentences, max 100 words) of this forum post.
                Focus on the main topic and key points. Be neutral and informative.
                Write the summary in the same language as the post content.
                
                Title: "%s"
                Content: "%s"
                
                Respond with ONLY the summary text, no JSON, no quotes, no formatting.
                """.formatted(
                    title != null ? title : "",
                    content != null ? content : ""
                );
            
            // Call Gemini API
            String summary = askGemini(prompt);
            
            // Clean up the response (remove any quotes or extra whitespace)
            summary = summary.trim();
            if (summary.startsWith("\"") && summary.endsWith("\"")) {
                summary = summary.substring(1, summary.length() - 1);
            }
            
            System.out.println("Summary generated successfully: " + summary.substring(0, Math.min(50, summary.length())) + "...");
            
            return PostSummaryResponse.success(summary, false);
            
        } catch (Exception e) {
            System.err.println("Error generating summary for post " + postId + ": " + e.getMessage());
            e.printStackTrace();
            return PostSummaryResponse.error("Failed to generate summary: " + e.getMessage());
        }
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
