package com.hcmus.forumus_backend.dto.post;

public class PostIdRequest {
    private String postId;

    public PostIdRequest() {
    }

    public PostIdRequest(String postId) {
        this.postId = postId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
