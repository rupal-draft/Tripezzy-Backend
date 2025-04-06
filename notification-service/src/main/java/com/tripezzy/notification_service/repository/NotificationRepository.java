package com.tripezzy.notification_service.repository;

import com.tripezzy.notification_service.entity.Notification;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Cacheable("notificationsByUserId")
    List<Notification> findByUserId(Long userId);
}
