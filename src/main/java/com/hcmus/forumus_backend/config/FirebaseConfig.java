package com.hcmus.forumus_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path}")
    private String serviceAccountPath;

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccountStream;

            // Check if path is a classpath resource or an absolute file path
            if (serviceAccountPath.startsWith("classpath:")) {
                Resource resource = resourceLoader.getResource(serviceAccountPath);
                serviceAccountStream = resource.getInputStream();
            } else {
                // Treat as absolute file path
                serviceAccountStream = new FileInputStream(serviceAccountPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();

            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    Firestore firestore() throws IOException {
        // Ensure Firebase is initialized first
        initializeFirebase();
        return FirestoreClient.getFirestore();
    }
}
