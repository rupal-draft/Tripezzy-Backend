package com.tripezzy.notification_service.service.implementations;

import com.tripezzy.notification_service.dto.NotificationDto;
import com.tripezzy.notification_service.exceptions.IllegalState;
import com.tripezzy.notification_service.repository.NotificationRepository;
import com.tripezzy.notification_service.service.NotificationService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {


    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository, ModelMapper modelMapper) {
        this.notificationRepository = notificationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Cacheable(value = "notificationsByUserId", key = "#userId")
    public List<NotificationDto> getNotificationsByUserId(Long userId) {
        log.info("Retrieving notifications for user: {}", userId);
        return notificationRepository
                .findByUserId(userId)
                .stream()
                .map(notification -> {
                    try{
                        NotificationDto notificationDto = modelMapper.map(notification, NotificationDto.class);
                        return notificationDto;
                    } catch (MappingException e) {
                        log.error("Error mapping notification: {}", notification, e);
                        throw new IllegalState("Failed to map notification data");
                    }
                }).toList();
    }
}
