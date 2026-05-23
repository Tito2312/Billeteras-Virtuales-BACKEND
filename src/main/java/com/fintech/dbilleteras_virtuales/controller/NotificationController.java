package com.fintech.dbilleteras_virtuales.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fintech.dbilleteras_virtuales.model.Notification;
import com.fintech.dbilleteras_virtuales.service.NotificationService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/queue/{userId}")
    public ResponseEntity<List<Notification>> getNotificationQueue(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.notificationQueue(userId).toList());
    }

}
