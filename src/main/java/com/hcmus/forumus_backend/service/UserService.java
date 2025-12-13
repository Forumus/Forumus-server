package com.hcmus.forumus_backend.service;

import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.hcmus.forumus_backend.model.User;

@Service
public class UserService {
    private final Firestore db;

    public UserService(Firestore firestore) {
        this.db = firestore;
    }

    public User getUserById(String userId) throws Exception {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        var docRef = db.collection("users").document(userId);
        var docSnap = docRef.get().get();

        if (docSnap.exists()) {
            var user = docSnap.toObject(User.class);

            if (user == null) {
                throw new Exception("Failed to convert document to User object for ID: " + userId);
            }
            
            return new User(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getProfilePictureUrl(),
                user.getFcmToken()
            );
        } else {
            throw new Exception("User not found with ID: " + userId);
        }
    }
}
