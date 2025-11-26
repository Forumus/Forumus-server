package com.hcmus.forumus_backend.dto.auth;

public class PasswordResetResponse {
    private boolean success;
    private String message;
    private String error;

    public PasswordResetResponse() {
    }

    public PasswordResetResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static PasswordResetResponse success(String message) {
        return new PasswordResetResponse(true, message);
    }

    public static PasswordResetResponse error(String error) {
        PasswordResetResponse response = new PasswordResetResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
