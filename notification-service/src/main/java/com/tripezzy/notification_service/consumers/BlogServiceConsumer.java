package com.tripezzy.notification_service.consumers;

import com.tripezzy.blog_service.events.BlogCommentedEvent;
import com.tripezzy.blog_service.events.BlogCreatedEvent;
import com.tripezzy.blog_service.events.BlogLikedEvent;
import com.tripezzy.notification_service.dto.UserDto;
import com.tripezzy.notification_service.entity.Notification;
import com.tripezzy.notification_service.grpc.UserGrpcClient;
import com.tripezzy.notification_service.repository.NotificationRepository;
import com.tripezzy.notification_service.utils.NotificationUtil;
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
public class BlogServiceConsumer {


    private static final Logger log = LoggerFactory.getLogger(BlogServiceConsumer.class);
    private static final String NEW_BLOG_TOPIC = "new-blog";
    private static final String BLOG_LIKED_TOPIC = "blog-liked";
    private static final String BLOG_COMMENTED_TOPIC = "blog-commented";

    private final UserGrpcClient usersClients;
    private final NotificationRepository notificationRepository;
    private final NotificationUtil notificationUtil;

    public BlogServiceConsumer(UserGrpcClient usersClients, NotificationRepository notificationRepository, NotificationUtil notificationUtil) {
        this.usersClients = usersClients;
        this.notificationRepository = notificationRepository;
        this.notificationUtil = notificationUtil;
    }

    @KafkaListener(topics = NEW_BLOG_TOPIC)
    public void handleNewBlog(BlogCreatedEvent blog) throws ServiceException {
        log.info("Received new blog event with Blog ID: {}", blog.getBlog());
        try {
            List<UserDto> users = usersClients.getAllUsers();
            for (UserDto user : users) {
                log.info("Sending notification to user: {}", user.getFirstName());
                String message = String.format("New blog created by %s with ID: %s", blog.getAuthor(), blog.getBlog());
                notificationUtil.sendNotification(user.getId(), message);
                log.info("Notification sent to user: {}", user.getFirstName());
            }
            log.info("Notification sent to all users");
        } catch (DataAccessException | TransactionSystemException ex) {
            log.error("Database error while saving notification: {}", ex.getMessage(), ex);
            throw new ServiceException("Notification saving failed", ex);

        } catch (Exception ex) {
            log.error("Unexpected error in booking event processing", ex);
            throw new ServiceException("Unexpected error", ex);
        }
    }

    @KafkaListener(topics = BLOG_LIKED_TOPIC)
    public void handleBlogLiked(BlogLikedEvent blog) throws ServiceException {
        log.info("Received blog liked event");
        try {
            UserDto user = usersClients.getUserById(blog.getUser());
            log.info("Sending notification to user: {}", user.getFirstName());
            String message = String.format("Your blog with ID %s has been liked", blog.getBlog());
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

    @KafkaListener(topics = BLOG_COMMENTED_TOPIC)
    public void handleBlogCommented(BlogCommentedEvent blog) throws ServiceException {
        log.info("Received blog commented event");
        try {
            UserDto user = usersClients.getUserById(blog.getUser());
            log.info("Sending notification to user: {}", user.getFirstName());
            String message = String.format("Your blog with ID %s has been commented", blog.getBlog());
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
