package com.hcmus.forumus_backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.hcmus.forumus_backend.dto.topic.TopicRequest;
import com.hcmus.forumus_backend.dto.topic.TopicResponse;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TopicService {
    private final Firestore db;

    public TopicService() {
        this.db = FirestoreClient.getFirestore();
    }

    public List<TopicResponse> getAllTopics() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = this.db.collection("topics").get();
        QuerySnapshot querySnapshot = future.get();

        List<TopicResponse> topics = new ArrayList<>();

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            TopicResponse topic = new TopicResponse(
                    document.getString("topic_id"),
                    document.getString("name"),
                    document.getString("description"));
            topics.add(topic);
        }

        System.out.println("Fetched Topics: " + topics);

        return topics;
    }

    public boolean addTopic(List<TopicRequest> topicRequests) {
        try {
            for (TopicRequest topicRequest : topicRequests) {
                String topicId = generateTopicId(topicRequest.getName());
                if (topicId == null || topicId.isEmpty())
                    continue;

                ApiFuture<WriteResult> future = db.collection("topics")
                        .document(topicId)
                        .set(topicRequest);
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
