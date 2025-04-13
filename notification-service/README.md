# Notification Service

This documentation outlines the available API endpoints for the Notification Service, which provides users with personalized notifications based on events occurring within the Tripezzy platform. This service consumes Kafka topics from other microservices to generate and store notifications.

## Features

* **Retrieve User Notifications:**
    * Allows users to fetch their own notifications.
* **Kafka Consumer:**
    * Consumes events from various Kafka topics to generate notifications:
        * `new-blog`: When a new blog post is created.
        * `blog-liked`: When a user likes a blog post.
        * `blog-commented`: When a user comments on a blog post.
        * `new-booking`: When a new booking is created.
        * `update-booking-status`: When the status of a booking is updated.
        * `booking-confirmed`: When a booking is confirmed.
        * `checkout-product`: When a product checkout is successful.
* **Database Storage:**
    * Stores notifications in a database for persistent retrieval.

## Technologies Used

* Spring Boot
* Spring Data JPA
* Hibernate
* Apache Kafka
* Lombok
* Validation API
* Spring Cloud Stream (for Kafka integration)
* `@CreationTimestamp` (for automatic timestamping)

## Entities and Database Schemas

The service uses the following entity, which translates to a database schema:

### `Notification`

| Column        | Data Type        | Constraints             | Description                                                                  |
|---------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the notification.                                    |
| `user_id`     | `BIGINT`         | `NOT NULL`, `INDEX`     | ID of the user to whom the notification belongs.                               |
| `message`     | `VARCHAR(255)`   | `NOT NULL`              | The content of the notification message.                                     |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the notification was created (automatically generated).         |

## API Endpoints

| Method | Endpoint | Description                                                                                                                                                                                              |
|--------|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GET`  | `/`      | Retrieves the authenticated user's notifications. Requires a valid user context (e.g., JWT).                                                                                                            |

## Kafka Topic Consumption

The Notification Service listens to the following Kafka topics and generates notifications based on the received events:

* **`new-blog`**:
    * **Event Description**: A new blog post has been created.
    * **Notification Example**: "A new blog post '{blog_title}' has been published!" (Target audience might be all users or specific subscribers).
* **`blog-liked`**:
    * **Event Description**: A user has liked a blog post.
    * **Notification Example**: "User '{liking_user}' liked your blog post '{blog_title}'!" (Target audience: author of the blog post).
* **`blog-commented`**:
    * **Event Description**: A user has commented on a blog post.
    * **Notification Example**: "User '{commenting_user}' commented on your blog post '{blog_title}': '{comment_content}'" (Target audience: author of the blog post and possibly other commenters).
* **`new-booking`**:
    * **Event Description**: A new booking has been created.
    * **Notification Example**: "Your booking for '{destination_name}' starting on '{start_date}' has been created." (Target audience: the user who made the booking).
* **`update-booking-status`**:
    * **Event Description**: The status of a booking has been updated.
    * **Notification Example**: "Your booking for '{destination_name}' has been updated to '{new_status}'." (Target audience: the user associated with the booking).
* **`booking-confirmed`**:
    * **Event Description**: A booking has been confirmed.
    * **Notification Example**: "Your booking for '{destination_name}' starting on '{start_date}' is now confirmed!" (Target audience: the user associated with the booking).
* **`checkout-product`**:
    * **Event Description**: A product checkout was successful.
    * **Notification Example**: "Your order #{order_id} has been placed successfully!" (Target audience: the user who made the purchase).

The specific content and targeting of these notifications might vary based on the application's requirements.

## Configuration

The service is configured using `application.properties` or `application.yml` (environment variable based). Ensure that your Kafka broker details are correctly configured for the service to consume events.

## Security

The `/` endpoint for retrieving notifications requires proper user authentication to ensure that users can only access their own notifications. This is typically handled by verifying a valid JWT or other authentication tokens in the request headers.