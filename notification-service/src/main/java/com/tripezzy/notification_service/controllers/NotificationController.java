package com.tripezzy.notification_service.controllers;

import com.tripezzy.notification_service.auth.UserContext;
import com.tripezzy.notification_service.auth.UserContextHolder;
import com.tripezzy.notification_service.dto.NotificationDto;
import com.tripezzy.notification_service.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/core")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMyNotifications(){
        UserContext userContext = UserContextHolder.getUserDetails();
        Long userId = userContext.getUserId();
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }
}
