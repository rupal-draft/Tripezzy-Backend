package com.tripezzy.notification_service.service;

import com.tripezzy.notification_service.dto.NotificationDto;

import java.util.List;

public interface NotificationService {

    List<NotificationDto> getNotificationsByUserId(Long userId);
}
