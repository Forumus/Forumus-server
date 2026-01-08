package com.hcmus.forumus_backend.dto.notification;

public class NotificationTriggerRequest {
    private String type; // UPVOTE, COMMENT, REPLY
    private String actorId;
    private String actorName;
    private String targetId; // Post ID or Comment ID
    private String targetUserId; // The user to notify
    private String previewText; // Snippet of post title or comment
    private String originalPostTitle;
    private String originalPostContent;
    private String rejectionReason;

    public NotificationTriggerRequest() {
    }

    public NotificationTriggerRequest(String type, String actorId, String actorName, String targetId, String targetUserId, String previewText, String originalPostTitle, String originalPostContent, String rejectionReason) {
        this.type = type;
        this.actorId = actorId;
        this.actorName = actorName;
        this.targetId = targetId;
        this.targetUserId = targetUserId;
        this.previewText = previewText;
        this.originalPostTitle = originalPostTitle;
        this.originalPostContent = originalPostContent;
        this.rejectionReason = rejectionReason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getPreviewText() {
        return previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }

    public String getOriginalPostTitle() {
        return originalPostTitle;
    }

    public void setOriginalPostTitle(String originalPostTitle) {
        this.originalPostTitle = originalPostTitle;
    }

    public String getOriginalPostContent() {
        return originalPostContent;
    }

    public void setOriginalPostContent(String originalPostContent) {
        this.originalPostContent = originalPostContent;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
