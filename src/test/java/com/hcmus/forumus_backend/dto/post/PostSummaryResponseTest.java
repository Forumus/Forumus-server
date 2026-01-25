package com.hcmus.forumus_backend.dto.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostSummaryResponse DTO.
 * Verifies constructors, factory methods, getters, setters, and edge cases.
 */
class PostSummaryResponseTest {

    @Test
    @DisplayName("Default constructor creates empty response")
    void defaultConstructor_CreatesEmptyResponse() {
        PostSummaryResponse response = new PostSummaryResponse();
        
        assertFalse(response.isSuccess());
        assertNull(response.getSummary());
        assertNull(response.getErrorMessage());
        assertFalse(response.isCached());
    }

    @Test
    @DisplayName("Parameterized constructor sets all fields correctly")
    void parameterizedConstructor_SetsAllFields() {
        PostSummaryResponse response = new PostSummaryResponse(true, "Test summary", null, true);
        
        assertTrue(response.isSuccess());
        assertEquals("Test summary", response.getSummary());
        assertNull(response.getErrorMessage());
        assertTrue(response.isCached());
    }

    @Test
    @DisplayName("success() factory method creates successful response")
    void successFactory_CreatesSuccessfulResponse() {
        String summary = "This is a test summary of the post content.";
        
        PostSummaryResponse response = PostSummaryResponse.success(summary, false);
        
        assertTrue(response.isSuccess());
        assertEquals(summary, response.getSummary());
        assertNull(response.getErrorMessage());
        assertFalse(response.isCached());
    }

    @Test
    @DisplayName("success() factory method with cached flag")
    void successFactory_WithCachedFlag() {
        String summary = "Cached summary";
        
        PostSummaryResponse response = PostSummaryResponse.success(summary, true);
        
        assertTrue(response.isSuccess());
        assertEquals(summary, response.getSummary());
        assertTrue(response.isCached());
    }

    @Test
    @DisplayName("error() factory method creates error response")
    void errorFactory_CreatesErrorResponse() {
        String errorMessage = "Post not found";
        
        PostSummaryResponse response = PostSummaryResponse.error(errorMessage);
        
        assertFalse(response.isSuccess());
        assertNull(response.getSummary());
        assertEquals(errorMessage, response.getErrorMessage());
        assertFalse(response.isCached());
    }

    @Test
    @DisplayName("Setters update fields correctly")
    void setters_UpdateFields() {
        PostSummaryResponse response = new PostSummaryResponse();
        
        response.setSuccess(true);
        response.setSummary("Updated summary");
        response.setErrorMessage("Updated error");
        response.setCached(true);
        
        assertTrue(response.isSuccess());
        assertEquals("Updated summary", response.getSummary());
        assertEquals("Updated error", response.getErrorMessage());
        assertTrue(response.isCached());
    }

    @Test
    @DisplayName("Handles empty summary")
    void emptySummary_IsAllowed() {
        PostSummaryResponse response = PostSummaryResponse.success("", false);
        
        assertTrue(response.isSuccess());
        assertEquals("", response.getSummary());
    }

    @Test
    @DisplayName("Handles long summary text")
    void longSummary_IsAllowed() {
        String longSummary = "This is a very detailed summary. ".repeat(100);
        
        PostSummaryResponse response = PostSummaryResponse.success(longSummary, false);
        
        assertTrue(response.isSuccess());
        assertEquals(longSummary, response.getSummary());
    }

    @Test
    @DisplayName("Handles unicode characters in summary")
    void unicodeSummary_IsAllowed() {
        String unicodeSummary = "这是一个测试摘要 🎉 с русскими буквами";
        
        PostSummaryResponse response = PostSummaryResponse.success(unicodeSummary, false);
        
        assertTrue(response.isSuccess());
        assertEquals(unicodeSummary, response.getSummary());
    }

    @Test
    @DisplayName("Error response with empty error message")
    void emptyErrorMessage_IsAllowed() {
        PostSummaryResponse response = PostSummaryResponse.error("");
        
        assertFalse(response.isSuccess());
        assertEquals("", response.getErrorMessage());
    }

    @Test
    @DisplayName("Error response with null error message")
    void nullErrorMessage_IsAllowed() {
        PostSummaryResponse response = PostSummaryResponse.error(null);
        
        assertFalse(response.isSuccess());
        assertNull(response.getErrorMessage());
    }
}
