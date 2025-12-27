package com.hcmus.forumus_backend.controller;

import com.hcmus.forumus_backend.dto.notification.NotificationTriggerRequest;
import com.hcmus.forumus_backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("")
    public ResponseEntity<String> triggerNotification(@RequestBody NotificationTriggerRequest request) {
        boolean success = notificationService.triggerNotification(request);
        if (success) {
            return ResponseEntity.ok("Notification triggered successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to trigger notification");
        }
    }
}
