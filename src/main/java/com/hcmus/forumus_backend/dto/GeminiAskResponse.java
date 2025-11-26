package com.hcmus.forumus_backend.dto;

public class GeminiAskResponse {
    private String response;

    public GeminiAskResponse() {
    }

    public GeminiAskResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
