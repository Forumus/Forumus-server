package com.hcmus.forumus_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;

@Configuration
public class GeminiConfig {

    @Value("${google.genai.api-key}")
    private String geminiApiKey;

    @Bean
    public Client geminiClient() {
        return Client.builder()
                .apiKey(geminiApiKey)
                .build();
    }

    @Bean
    public GenerateContentConfig generateContentConfig() {
        return GenerateContentConfig.builder().build();
    }
}
