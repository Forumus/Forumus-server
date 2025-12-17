package com.hcmus.forumus_backend.service;

import java.util.Map;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.hcmus.forumus_backend.dto.topic.TopicRequest;
import com.hcmus.forumus_backend.dto.topic.TopicResponse;
import com.hcmus.forumus_backend.listener.TopicsListener;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {
    private final Firestore db;
    private final TopicsListener topicsListener;

    public TopicService(Firestore db, TopicsListener topicsListener) {
        this.db = db;
        this.topicsListener = topicsListener;
    }

    public Map<String, Object> getAllTopics() {
        try {
            List<TopicResponse> topics = topicsListener.getAllTopics();

            return Map.of(
                    "success", true,
                    "topics", topics);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "topics", List.of());
        }
    }

    public boolean addTopic(List<TopicRequest> topicRequests) {
        try {
            for (TopicRequest topicRequest : topicRequests) {
                String topicId = generateTopicId(topicRequest.getName());
                TopicResponse topicResponse = new TopicResponse(
                        topicId,
                        topicRequest.getName(),
                        topicRequest.getDescription());

                if (topicId == null || topicId.isEmpty())
                    continue;

                ApiFuture<WriteResult> future = db.collection("topics")
                        .document(topicId)
                        .set(topicResponse);
                System.out.println("Update time : " + future.get().getUpdateTime());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateTopicId(String name) {
        return name.toLowerCase().replaceAll("\\s+", "_");
    }
}
