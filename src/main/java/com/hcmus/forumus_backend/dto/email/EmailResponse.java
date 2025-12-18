package com.hcmus.forumus_backend.dto.email;

public class EmailResponse {
    private boolean success;
    private String message;

    public EmailResponse() {
    }

    public EmailResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static EmailResponse success(String message) {
        return new EmailResponse(true, message);
    }

    public static EmailResponse error(String message) {
        return new EmailResponse(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
