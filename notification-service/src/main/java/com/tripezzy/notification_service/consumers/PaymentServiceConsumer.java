package com.tripezzy.notification_service.consumers;

import com.tripezzy.notification_service.dto.UserDto;
import com.tripezzy.notification_service.grpc.UserGrpcClient;
import com.tripezzy.notification_service.repository.NotificationRepository;
import com.tripezzy.notification_service.utils.NotificationUtil;
import com.tripezzy.payment_service.events.CheckoutProductEvent;
import org.apache.kafka.shaded.com.google.protobuf.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.util.List;

@Service
public class PaymentServiceConsumer {


    private static final Logger log = LoggerFactory.getLogger(PaymentServiceConsumer.class);
    private static final String NEW_PAYMENT_TOPIC = "checkout-product";

    private final NotificationRepository notificationRepository;
    private final NotificationUtil notificationUtil;
    private final UserGrpcClient userGrpcClient;

    public PaymentServiceConsumer(NotificationRepository notificationRepository, NotificationUtil notificationUtil, UserGrpcClient userGrpcClient) {
        this.notificationRepository = notificationRepository;
        this.notificationUtil = notificationUtil;
        this.userGrpcClient = userGrpcClient;
    }

    @KafkaListener(topics = NEW_PAYMENT_TOPIC)
    public void handleCheckoutProduct(CheckoutProductEvent event) throws ServiceException {
        log.info("Received CheckoutProductEvent: {}", event);
        try{
            UserDto user = userGrpcClient.getUserById(event.getUser());
            log.info("Sending notification to : {} with ID : {}", user.getFirstName(), user.getId());
            String message = notificationUtil.buildSellerNotification(event);
            notificationUtil.sendNotification(user.getId(), message);
            log.info("Notification sent to user: {}", user.getFirstName());
        } catch (DataAccessException | TransactionSystemException ex) {
            log.error("Database error while saving notification: {}", ex.getMessage(), ex);
            throw new ServiceException("Notification saving failed", ex);
        } catch (Exception ex) {
            log.error("Unexpected error in booking event processing", ex);
            throw new ServiceException("Unexpected error", ex);
        }
    }
}
