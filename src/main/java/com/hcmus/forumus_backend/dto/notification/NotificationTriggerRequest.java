package com.hcmus.forumus_backend.dto.notification;

public class NotificationTriggerRequest {
    private String type; // UPVOTE, COMMENT, REPLY
    private String actorId;
    private String actorName;
    private String targetId; // Post ID or Comment ID
    private String targetUserId; // The user to notify
    private String previewText; // Snippet of post title or comment

    public NotificationTriggerRequest() {
    }

    public NotificationTriggerRequest(String type, String actorId, String actorName, String targetId, String targetUserId, String previewText) {
        this.type = type;
        this.actorId = actorId;
        this.actorName = actorName;
        this.targetId = targetId;
        this.targetUserId = targetUserId;
        this.previewText = previewText;
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
}
