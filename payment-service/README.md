# Payment Service Microservice

This repository contains the code for a Payment Service microservice, built using Spring Boot. This service handles payment processing for both e-commerce purchases and booking transactions.

## Features

* **Payment Processing:**
    * Handles checkout for shopping carts using gRPC communication with the Cart Service.
    * **Stripe Integration:** Uses Stripe for secure payment processing.
    * Handles checkout for bookings using gRPC communication with the Booking Service.
    * Stores payment information in a database.
* **Payment Retrieval:**
    * Retrieves all payment records.
    * Retrieves payment records for a specific user.
* **Role-Based Access Control (RBAC):**
    * Restricted access to administrative endpoints using custom `@RoleRequired("ADMIN")` annotation.
* **Rate Limiting:**
    * Implemented rate limiting using Resilience4j to prevent abuse.
* **Database:**
    * Uses a relational database (configured via environment variables).
* **Auditing:**
    * Manages created and updated timestamps.

## Technologies Used

* Spring Boot
* Spring Data JPA
* Resilience4j (for rate limiting)
* Lombok
* Hibernate
* Validation API
* gRPC
* Protobuf
* **Stripe Java Library**

## Entities and Database Schemas

The service uses the following entity, which translates to a database schema:

### `Payment`

| Column        | Data Type        | Constraints             | Description                                                                  |
|---------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the payment.                                        |
| `user_id`     | `BIGINT`         | `NOT NULL`, `INDEX`     | ID of the user associated with the payment.                                  |
| `reference`   | `BIGINT`         | `NOT NULL`              | Reference ID (cart ID or booking ID).                                         |
| `session`     | `VARCHAR(512)`   | `NOT NULL`, `UNIQUE`      | Session ID for the payment.                                                  |
| `sessionUrl`  | `VARCHAR(1024)`  | `NOT NULL`              | Session URL for the payment.                                                  |
| `status`      | `VARCHAR(255)`   | `NOT NULL`              | Status of the payment (e.g., `SUCCESS`, `FAILED`).                           |
| `amount`      | `BIGINT`         | `NOT NULL`              | Amount of the payment.                                                        |
| `currency`    | `VARCHAR(255)`   | `NOT NULL`              | Currency of the payment.                                                      |
| `name`        | `VARCHAR(255)`   | `NOT NULL`              | Name of the payment.                                                          |
| `category`    | `VARCHAR(255)`   | `NOT NULL`              | Category of the payment (e.g., `SHOP`, `BOOKING`).                           |
| `quantity`    | `BIGINT`         | `NOT NULL`              | Quantity of items or bookings.                                               |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the payment was created.                                       |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the payment was last updated.                                   |

## API Endpoints

| Method | Endpoint                    | Description                                                                                                                                                                                              |
|--------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/core/checkout/shop/{cartId}` | Processes checkout for a shopping cart.                                                                                                                                                                |
| `POST` | `/core/checkout/bookings/{bookingId}` | Processes checkout for a booking.                                                                                                                                                                 |
| `GET`  | `/core`                     | Retrieves all payment records.                                                                                                                                                                          |
| `GET`  | `/core/user/{userId}`        | Retrieves payment records for a specific user.                                                                                                                                                           |

## Configuration

The service is configured using `application.properties` or `application.yml` (environment variable based).

## Getting Started

1.  **Clone the repository:**

    ```bash
    git clone [repository URL]
    ```

2.  **Build the project:**

    ```bash
    ./mvnw clean install
    ```

3.  **Run the application:**

    ```bash
    ./mvnw spring-boot:run
    ```

4.  **Configure environment variables:**
    * Set the database credentials and other configuration properties in your environment.
    * Configure gRPC client properties for connecting to the Cart and Booking services.

## Rate Limiter configuration.

The Rate limiter is set with these parameters.
```yaml
resilience4j:
  ratelimiter:
    instances:
      checkoutProducts:
        limitForPeriod: 10 # Number of requests allowed per period
        limitRefreshPeriod: 10s # Period in seconds for refreshing limits
        timeoutDuration: 1s # Timeout for acquiring a permission
      checkoutBookings:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      paymentsRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      userPaymentsRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
   ```

## Stripe Configuration

Ensure that you have your Stripe API keys configured in your environment variables or application properties. This is crucial for successful payment processing.

* Example application.properties configuration:

```properties
stripe.api.key={STRIPE_SECRET_KEY}
stripe.api.secret={STRIPE_PUBLISHED_KEY}
```

## Stripe Configuration Class:

```java
@Configuration
public class StripeConfig {

    @Value("${stripe.secret}")
    private String stripeSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecret;
    }
}
```
## Role-Based Access Control (RBAC)
Administrative endpoints are protected using a custom `@RoleRequired("ADMIN")` annotation. This annotation ensures that only users with the "ADMIN" role can access these endpoints. You'll need to implement the logic for validating user roles based on your authentication and authorization mechanism.
