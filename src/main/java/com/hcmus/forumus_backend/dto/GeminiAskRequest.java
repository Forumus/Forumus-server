package com.hcmus.forumus_backend.dto;

public class GeminiAskRequest {
    private String prompt;

    public GeminiAskRequest() {
    }

    public GeminiAskRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
