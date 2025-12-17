package com.hcmus.forumus_backend.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import com.hcmus.forumus_backend.dto.topic.TopicRequest;
import com.hcmus.forumus_backend.service.TopicService;

@RestController
@RequestMapping("/api/topics")
@CrossOrigin
public class TopicController {

    private final TopicService TopicService;

    public TopicController(TopicService TopicService) {
        this.TopicService = TopicService;
    }

    @GetMapping("/getAll")
    public Map<String, Object> getAllTopics() {
        return TopicService.getAllTopics();
    }

    @PostMapping("/add")
    public Map<String, Boolean> addTopic(@RequestBody List<TopicRequest> topicRequests) {
        return Map.of("success", TopicService.addTopic(topicRequests));
    }
}
