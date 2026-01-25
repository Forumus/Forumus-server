package com.hcmus.forumus_backend.dto.post;

/**
 * Response DTO for AI-generated post summaries with caching support.
 * 
 * <p>The contentHash field allows clients to detect when content has changed
 * and invalidate their local cache accordingly.</p>
 */
public class PostSummaryResponse {
    private boolean success;
    private String summary;
    private String errorMessage;
    private boolean cached;
    private String contentHash;  // Hash of post content for cache validation
    private Long generatedAt;    // Timestamp when summary was generated
    private Long expiresAt;      // Timestamp when cache expires

    public PostSummaryResponse() {
    }

    public PostSummaryResponse(boolean success, String summary, String errorMessage, boolean cached) {
        this.success = success;
        this.summary = summary;
        this.errorMessage = errorMessage;
        this.cached = cached;
    }

    public PostSummaryResponse(boolean success, String summary, String errorMessage, boolean cached, 
                                String contentHash, Long generatedAt, Long expiresAt) {
        this.success = success;
        this.summary = summary;
        this.errorMessage = errorMessage;
        this.cached = cached;
        this.contentHash = contentHash;
        this.generatedAt = generatedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Factory method for successful summary response with cache metadata.
     */
    public static PostSummaryResponse success(String summary, boolean cached, String contentHash, Long generatedAt) {
        // Cache expires in 24 hours
        Long expiresAt = generatedAt != null ? generatedAt + (24 * 60 * 60 * 1000) : null;
        return new PostSummaryResponse(true, summary, null, cached, contentHash, generatedAt, expiresAt);
    }

    /**
     * Factory method for successful summary response (legacy support).
     */
    public static PostSummaryResponse success(String summary, boolean cached) {
        return new PostSummaryResponse(true, summary, null, cached, null, System.currentTimeMillis(), null);
    }

    /**
     * Factory method for error response.
     */
    public static PostSummaryResponse error(String errorMessage) {
        return new PostSummaryResponse(false, null, errorMessage, false, null, null, null);
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

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
