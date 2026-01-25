package com.hcmus.forumus_backend.dto.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostSummaryRequest DTO.
 * Verifies constructor, getters, setters, and edge cases.
 */
class PostSummaryRequestTest {

    @Test
    @DisplayName("Default constructor creates empty request")
    void defaultConstructor_CreatesEmptyRequest() {
        PostSummaryRequest request = new PostSummaryRequest();
        
        assertNull(request.getPostId());
    }

    @Test
    @DisplayName("Parameterized constructor sets postId correctly")
    void parameterizedConstructor_SetsPostId() {
        String postId = "test-post-123";
        
        PostSummaryRequest request = new PostSummaryRequest(postId);
        
        assertEquals(postId, request.getPostId());
    }

    @Test
    @DisplayName("setPostId updates the postId value")
    void setPostId_UpdatesValue() {
        PostSummaryRequest request = new PostSummaryRequest();
        String postId = "updated-post-456";
        
        request.setPostId(postId);
        
        assertEquals(postId, request.getPostId());
    }

    @Test
    @DisplayName("Handles empty string postId")
    void emptyString_PostId() {
        PostSummaryRequest request = new PostSummaryRequest("");
        
        assertEquals("", request.getPostId());
    }

    @Test
    @DisplayName("Handles null postId")
    void nullPostId_IsAllowed() {
        PostSummaryRequest request = new PostSummaryRequest(null);
        
        assertNull(request.getPostId());
    }

    @Test
    @DisplayName("Handles long postId")
    void longPostId_IsAllowed() {
        String longId = "a".repeat(1000);
        PostSummaryRequest request = new PostSummaryRequest(longId);
        
        assertEquals(longId, request.getPostId());
        assertEquals(1000, request.getPostId().length());
    }

    @Test
    @DisplayName("Handles special characters in postId")
    void specialCharacters_InPostId() {
        String specialId = "post-123_abc@#$%";
        PostSummaryRequest request = new PostSummaryRequest(specialId);
        
        assertEquals(specialId, request.getPostId());
    }
}
