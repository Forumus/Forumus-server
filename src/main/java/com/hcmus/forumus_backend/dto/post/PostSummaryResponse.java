package com.hcmus.forumus_backend.dto.post;

/**
 * Response DTO for AI-generated post summaries.
 */
public class PostSummaryResponse {
    private boolean success;
    private String summary;
    private String errorMessage;
    private boolean cached;

    public PostSummaryResponse() {
    }

    public PostSummaryResponse(boolean success, String summary, String errorMessage, boolean cached) {
        this.success = success;
        this.summary = summary;
        this.errorMessage = errorMessage;
        this.cached = cached;
    }

    /**
     * Factory method for successful summary response.
     */
    public static PostSummaryResponse success(String summary, boolean cached) {
        return new PostSummaryResponse(true, summary, null, cached);
    }

    /**
     * Factory method for error response.
     */
    public static PostSummaryResponse error(String errorMessage) {
        return new PostSummaryResponse(false, null, errorMessage, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }
}
