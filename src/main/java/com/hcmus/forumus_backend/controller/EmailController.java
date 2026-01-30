package com.hcmus.forumus_backend.controller;

import com.hcmus.forumus_backend.dto.email.EmailRequest;
import com.hcmus.forumus_backend.dto.email.EmailResponse;
import com.hcmus.forumus_backend.dto.email.WelcomeEmailRequest;
import com.hcmus.forumus_backend.dto.email.ReportEmailRequest;
import com.hcmus.forumus_backend.enums.UserStatus;
import com.hcmus.forumus_backend.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<EmailResponse> sendOTPEmail(@RequestBody EmailRequest request) {
        // Validate input
        if (request.getRecipientEmail() == null || request.getRecipientEmail().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EmailResponse.error("Recipient email is required"));
        }

        if (request.getOtpCode() == null || request.getOtpCode().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EmailResponse.error("OTP code is required"));
        }

        try {
            // Send OTP email
            boolean sent = emailService.sendOTPEmail(request.getRecipientEmail(), request.getOtpCode());

            if (sent) {
                return ResponseEntity.ok(EmailResponse.success("OTP email sent successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(EmailResponse.error("Failed to send OTP email"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponse.error("Error sending OTP email: " + e.getMessage()));
        }
    }

    @PostMapping("/send-welcome")
    public ResponseEntity<EmailResponse> sendWelcomeEmail(@RequestBody WelcomeEmailRequest request) {
        // Validate input
        if (request.getRecipientEmail() == null || request.getRecipientEmail().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EmailResponse.error("Recipient email is required"));
        }

        if (request.getUserName() == null || request.getUserName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EmailResponse.error("User name is required"));
        }

        try {
            // Send welcome email
            boolean sent = emailService.sendWelcomeEmail(request.getRecipientEmail(), request.getUserName());

            if (sent) {
                return ResponseEntity.ok(EmailResponse.success("Welcome email sent successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(EmailResponse.error("Failed to send welcome email"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponse.error("Error sending welcome email: " + e.getMessage()));
        }
    }

    @PostMapping("/send-report")
    public ResponseEntity<EmailResponse> sendReportEmail(@RequestBody ReportEmailRequest request) {
        // Validate input
        if (request.getRecipientEmail() == null || request.getRecipientEmail().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EmailResponse.error("Recipient email is required"));
        }

        if (request.getUserName() == null || request.getUserName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EmailResponse.error("User name is required"));
        }

        if (request.getUserStatus() == null || request.getUserStatus().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EmailResponse.error("User status is required"));
        }

        try {
            // Parse user status
            UserStatus userStatus = UserStatus.fromString(request.getUserStatus());

            // Send report email
            boolean sent = emailService.sendReportEmail(
                    request.getRecipientEmail(),
                    request.getUserName(),
                    userStatus,
                    request.getReportedPosts());

            if (sent) {
                return ResponseEntity.ok(EmailResponse.success("Report email sent successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(EmailResponse.error("Failed to send report email"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EmailResponse.error("Invalid user status: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponse.error("Error sending report email: " + e.getMessage()));
        }
    }
}
