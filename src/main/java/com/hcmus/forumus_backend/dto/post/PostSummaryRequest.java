package com.hcmus.forumus_backend.dto.post;

public class PostSummaryRequest {
    private String postId;

    public PostSummaryRequest() {
    }

    public PostSummaryRequest(String postId) {
        this.postId = postId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
