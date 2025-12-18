package com.hcmus.forumus_backend.dto.email;

public class EmailRequest {
    private String recipientEmail;
    private String otpCode;

    public EmailRequest() {
    }

    public EmailRequest(String recipientEmail, String otpCode) {
        this.recipientEmail = recipientEmail;
        this.otpCode = otpCode;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
