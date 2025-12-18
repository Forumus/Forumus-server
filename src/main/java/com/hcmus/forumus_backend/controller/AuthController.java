package com.hcmus.forumus_backend.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.hcmus.forumus_backend.dto.auth.PasswordResetRequest;
import com.hcmus.forumus_backend.dto.auth.PasswordResetResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Value("${admin.secret.key}")
    private String adminSecretKey;

    @PostMapping("/resetPassword")
    public ResponseEntity<PasswordResetResponse> resetPassword(@RequestBody PasswordResetRequest request) {
        // Security check
        if (!adminSecretKey.equals(request.getSecretKey())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(PasswordResetResponse.error("Unauthorized !!"));
        }

        // Validate input
        if (request.getEmail() == null || request.getEmail().isEmpty() ||
                request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PasswordResetResponse.error("Missing email or newPassword"));
        }

        try {
            // Get user by email
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(request.getEmail());

            // Update password
            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(user.getUid())
                    .setPassword(request.getNewPassword());

            FirebaseAuth.getInstance().updateUser(updateRequest);

            return ResponseEntity.ok(PasswordResetResponse.success("Password updated"));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PasswordResetResponse.error(e.getMessage()));
        }
    }
}
