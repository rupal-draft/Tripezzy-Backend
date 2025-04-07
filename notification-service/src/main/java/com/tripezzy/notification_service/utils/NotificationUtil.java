package com.tripezzy.notification_service.utils;

import com.tripezzy.notification_service.entity.Notification;
import com.tripezzy.notification_service.repository.NotificationRepository;
import com.tripezzy.payment_service.events.CheckoutProductEvent;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;

@Component
public class NotificationUtil {


    private static final Logger log = LoggerFactory.getLogger(NotificationUtil.class);
    private final NotificationRepository notificationRepository;

    public NotificationUtil(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(Long userId, String message) {

        try{
            if (userId == null || message == null || message.isBlank()) {
                log.warn("Invalid notification request: userId={}, message={}", userId, message);
                return;
            }

            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setMessage(message);
            notificationRepository.save(notification);

            log.info("Saved notification for user {}: {}", userId, message);
        } catch (DataAccessException ex) {
            log.error("Database access error while saving notification", ex.getMessage());
        } catch (ConstraintViolationException ex) {
            log.error("Constraint violation while saving notification", ex.getMessage());
        } catch (TransactionSystemException ex) {
            log.error("Transaction error while saving notification", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while saving notification", ex.getMessage());
        }

    }

    public static String buildSellerNotification(CheckoutProductEvent payment) {
        return String.format(
                "🛒 New Order created!\n\n" +
                        "Product: %s\n" +
                        "Quantity: %d\n" +
                        "Amount: %.2f\n" +
                        "User ID: %d\n" +
                        "Product ID: %d\n" +
                        "Reference ID: %d\n" +
                        "Session ID: %s\n" +
                        "Track Order: %s",
                payment.getProductName(),
                payment.getQuantity(),
                payment.getAmount(),
                payment.getUser(),
                payment.getProduct(),
                payment.getReference(),
                payment.getSession(),
                payment.getSessionUrl()
        );
    }
}
