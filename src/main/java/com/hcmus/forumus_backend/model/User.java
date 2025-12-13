package com.hcmus.forumus_backend.model;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String profilePictureUrl;
    private String fcmToken;

    public User() {
    }

    public User(String userId, String fullName, String email, String profilePictureUrl, String fcmToken) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
        this.fcmToken = fcmToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
}
