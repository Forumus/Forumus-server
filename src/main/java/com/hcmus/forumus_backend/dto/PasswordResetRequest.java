package com.hcmus.forumus_backend.dto;

public class PasswordResetRequest {
    private String secretKey;
    private String email;
    private String newPassword;

    public PasswordResetRequest() {
    }

    public PasswordResetRequest(String secretKey, String email, String newPassword) {
        this.secretKey = secretKey;
        this.email = email;
        this.newPassword = newPassword;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
