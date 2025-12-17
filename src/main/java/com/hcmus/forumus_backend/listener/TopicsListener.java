package com.hcmus.forumus_backend.listener;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.hcmus.forumus_backend.dto.topic.TopicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class TopicsListener {

    private static final Logger logger = LoggerFactory.getLogger(TopicsListener.class);

    @Autowired
    private Firestore firestore;

    private final Map<String, TopicResponse> topicsCache = new ConcurrentHashMap<>();
    private ListenerRegistration listenerRegistration;

    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing topics cache...");

            // Fetch all existing topics
            ApiFuture<QuerySnapshot> future = firestore.collection("topics").get();
            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                TopicResponse topic = new TopicResponse(
                        document.getId(),
                        document.getString("name"),
                        document.getString("description"));
                topicsCache.put(topic.getTopicId(), topic);
            }

            logger.info("Loaded {} topics into cache", topicsCache.size());

            // Start listening for changes
            startListening();
        } catch (Exception e) {
            logger.error("Failed to initialize topics cache", e);
        }
    }

    private void startListening() {
        listenerRegistration = firestore.collection("topics")
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    logger.error("Error listening to topics collection", error);
                    return;
                }

                if (querySnapshot != null) {
                    for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                        QueryDocumentSnapshot document = change.getDocument();
                        String topicId = document.getId();

                        switch (change.getType()) {
                            case ADDED:
                                TopicResponse newTopic = new TopicResponse(
                                        topicId,
                                        document.getString("name"),
                                        document.getString("description"));
                                topicsCache.put(topicId, newTopic);
                                logger.info("Topic added to cache: {}", topicId);
                                break;

                            case MODIFIED:
                                TopicResponse modifiedTopic = new TopicResponse(
                                        topicId,
                                        document.getString("name"),
                                        document.getString("description"));
                                topicsCache.put(topicId, modifiedTopic);
                                logger.info("Topic updated in cache: {}", topicId);
                                break;

                            case REMOVED:
                                topicsCache.remove(topicId);
                                logger.info("Topic removed from cache: {}", topicId);
                                break;
                        }
                    }
                }
            });

        logger.info("Topics listener started successfully");
    }

    public List<TopicResponse> getAllTopics() {
        return new ArrayList<>(topicsCache.values());
    }

    public TopicResponse getTopicById(String topicId) {
        return topicsCache.get(topicId);
    }

    @PreDestroy
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            logger.info("Topics listener stopped");
        }
    }
}
