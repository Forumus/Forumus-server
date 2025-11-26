package com.hcmus.forumus_backend.dto.topic;

public class TopicRequest {
    private String name;
    private String description;

    public TopicRequest() {
    }

    public TopicRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
