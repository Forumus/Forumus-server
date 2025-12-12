package com.hcmus.forumus_backend.dto.post;

public class PostDTO {
    private String postId;
    private String title;
    private String content;

    public PostDTO() {
    }

    public PostDTO(String postId, String title, String content) {
        this.postId = postId;
        this.title = title;
        this.content = content;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
}

