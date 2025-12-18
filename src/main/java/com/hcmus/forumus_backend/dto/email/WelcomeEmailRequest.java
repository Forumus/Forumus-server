package com.hcmus.forumus_backend.dto.email;

public class WelcomeEmailRequest {
    private String recipientEmail;
    private String userName;

    public WelcomeEmailRequest() {
    }

    public WelcomeEmailRequest(String recipientEmail, String userName) {
        this.recipientEmail = recipientEmail;
        this.userName = userName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
