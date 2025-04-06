package com.tripezzy.notification_service.consumers;


import com.tripezzy.booking_service.events.BookingConfirmedEvent;
import com.tripezzy.booking_service.events.BookingCreatedEvent;
import com.tripezzy.booking_service.events.BookingStatusUpdatedEvent;
import com.tripezzy.notification_service.dto.UserDto;
import com.tripezzy.notification_service.entity.Notification;
import com.tripezzy.notification_service.grpc.UserGrpcClient;
import com.tripezzy.notification_service.repository.NotificationRepository;
import org.apache.kafka.shaded.com.google.protobuf.ServiceException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.util.List;

@Service
public class BookingServiceConsumer {


    private static final Logger log = LoggerFactory.getLogger(BookingServiceConsumer.class);
    private static final String NEW_BOOKING_TOPIC = "new-booking";
    private static final String STATUS_UPDATED_BOOKING_TOPIC = "update-booking-status";
    private static final String CONFIRMED_BOOKING_TOPIC = "booking-confirmed";
    private final UserGrpcClient usersClients;
    private final NotificationRepository notificationRepository;

    public BookingServiceConsumer(UserGrpcClient usersClients, NotificationRepository notificationRepository) {
        this.usersClients = usersClients;
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = NEW_BOOKING_TOPIC)
    public void handleNewBooking(BookingCreatedEvent event) throws ServiceException {
        log.info("Received new booking event with Booking ID: {}", event.getBooking());

        try {
            List<UserDto> admins = usersClients
                    .getAllAdminUsers();
            for (UserDto admin : admins) {
                log.info("Sending notification to admin: {}", admin.getFirstName());
                String message = String.format("New booking received with ID: %s", event.getBooking());
                sendNotification(admin, message);
                log.trace("Notification sent to admin: {}", admin.getFirstName());
            }
            log.trace("Notification sent to all admins");
        } catch (DataAccessException | TransactionSystemException ex) {
            log.error("Database error while saving notification: {}", ex.getMessage(), ex);
            throw new ServiceException("Notification saving failed", ex);

        } catch (Exception ex) {
            log.error("Unexpected error in booking event processing", ex);
            throw new ServiceException("Unexpected error", ex);
        }
    }

    @KafkaListener(topics = STATUS_UPDATED_BOOKING_TOPIC)
    public void handleStatusUpdate(BookingStatusUpdatedEvent event) throws ServiceException {
        log.info("Received status update event with Booking ID for User Id: {}: {}", event.getUser(), event.getBooking());
        try {
            UserDto user = usersClients.getUserById(event.getUser());
            log.info("Sending notification to : {} with ID : {}", user.getFirstName(), user.getId());
            String message = String.format(
                    "Your booking with ID %s has been updated to status: %s",
                    event.getBooking(),
                    event.getStatus()
            );
            sendNotification(user, message);
            log.trace("Notification sent to user: {}", user.getFirstName());
        } catch (DataAccessException | TransactionSystemException ex) {
            log.error("Database error while saving notification: {}", ex.getMessage(), ex);
            throw new ServiceException("Notification saving failed", ex);
        } catch (Exception ex) {
            log.error("Unexpected error in booking event processing", ex);
            throw new ServiceException("Unexpected error", ex);
        }
    }

    @KafkaListener(topics = CONFIRMED_BOOKING_TOPIC)
    public void handleConfirmedBooking(BookingConfirmedEvent event) throws ServiceException {
        log.info("Received confirmed booking event with Booking ID: {}", event.getBooking());
        try {
            UserDto user = usersClients.getUserById(event.getUser());
            log.info("Sending notification to : {} with ID : {}", user.getFirstName(), user.getId());
            String message = String.format(
                    "Your booking with ID %s has been confirmed",
                    event.getBooking()
            );
            sendNotification(user, message);
            log.trace("Notification sent to: {}", user.getFirstName());
        } catch (DataAccessException | TransactionSystemException ex) {
            log.error("Database error while saving notification: {}", ex.getMessage(), ex);
            throw new ServiceException("Notification saving failed", ex);
        } catch (Exception ex) {
            log.error("Unexpected error in booking event processing", ex);
            throw new ServiceException("Unexpected error", ex);
        }
    }

    private void sendNotification(UserDto user, String message) {
        log.info("Saving notification for User with ID: {}", user.getId());
        try {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setMessage(message);

            notificationRepository.save(notification);
            log.info("Notification saved for user: {}", user.getEmail());

        } catch (DataAccessException ex) {
            log.error("Database access error while saving notification for user {}: {}", user.getEmail(), ex.getMessage());
        } catch (ConstraintViolationException ex) {
            log.error("Constraint violation while saving notification for user {}: {}", user.getEmail(), ex.getMessage());
        } catch (TransactionSystemException ex) {
            log.error("Transaction error while saving notification for user {}: {}", user.getEmail(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while saving notification for user {}: {}", user.getEmail(), ex.getMessage());
        }
    }
}
