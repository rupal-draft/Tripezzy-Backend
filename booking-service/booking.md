# Booking Microservice

## Overview
This microservice handles all booking-related operations, including creating, retrieving, updating, and deleting bookings. It also supports filtering, pagination, and rate limiting using Resilience4j.

## Technologies Used
- **Spring Boot**
- **Spring Data JPA**
- **Resilience4j (Rate Limiting)**
- **PostgreSQL**
- **Restful Api**

## API Endpoints

### Booking Endpoints
| Method | Endpoint | Description | Rate Limit |
|--------|---------|-------------|------------|
| `POST` | `/bookings` | Create a new booking | 10 requests/sec |
| `GET` | `/bookings/{bookingId}` | Get booking by ID | 15 requests/sec |
| `GET` | `/bookings` | Get all bookings | 20 requests/sec |
| `GET` | `/bookings/paginated` | Get paginated bookings | 15 requests/sec |
| `GET` | `/bookings/user/{userId}` | Get bookings by user ID | 10 requests/sec |
| `GET` | `/bookings/destination/{destinationId}` | Get bookings by destination ID | 10 requests/sec |
| `GET` | `/bookings/status/{status}` | Get bookings by status | 10 requests/sec |
| `GET` | `/bookings/travel-date-range` | Get bookings by travel date range | 10 requests/sec |
| `GET` | `/bookings/payment-status-price-range` | Get bookings by payment status and price range | 10 requests/sec |
| `GET` | `/bookings/upcoming/{status}` | Get upcoming bookings by status | 10 requests/sec |
| `GET` | `/bookings/count-by-status/{status}` | Count bookings by status | 10 requests/sec |
| `DELETE` | `/bookings/{bookingId}` | Cancel a booking | 5 requests/sec |
| `DELETE` | `/bookings/soft-delete/{bookingId}` | Soft delete a booking | 5 requests/sec |
| `GET` | `/bookings/reference/{bookingReference}` | Get booking by reference | 15 requests/sec |
| `GET` | `/bookings/payment-status/{paymentStatus}` | Get bookings by payment status | 10 requests/sec |
| `PATCH` | `/bookings/{bookingId}/confirm` | Confirm a booking | 10 requests/sec |
| `GET` | `/bookings/filter` | Filter bookings | 10 requests/sec |

## Database Schema

### `bookings` Table
| Column | Type | Description |
|--------|------|-------------|
| `booking_id` | `BIGINT` | Primary key |
| `first_name` | `VARCHAR(50)` | First name of the user |
| `last_name` | `VARCHAR(50)` | Last name of the user |
| `email` | `VARCHAR` | Email address of the user |
| `phone_number` | `VARCHAR(15)` | Contact phone number (unique) |
| `user_id` | `BIGINT` | Foreign key to `users` table |
| `destination_id` | `BIGINT` | Foreign key to `destinations` table |
| `booking_date` | `TIMESTAMP` | Timestamp when booking was created |
| `travel_date` | `DATE` | Date of travel |
| `booking_status` | `ENUM` | Booking status (CONFIRMED, PENDING, CANCELLED) |
| `total_price` | `DECIMAL` | Total price of the booking |
| `payment_status` | `VARCHAR` | Payment status (PAID, UNPAID) |
| `booking_reference` | `VARCHAR` | Unique booking reference |
| `created_at` | `TIMESTAMP` | Timestamp when booking was created |
| `updated_at` | `TIMESTAMP` | Timestamp when booking was last updated |
| `deleted` | `BOOLEAN` | Soft deletion flag |

## Resilience4j Rate Limiting Configuration
```yaml
resilience4j:
  ratelimiter:
    instances:
      defaultRateLimiter:
        limitForPeriod: 20
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      paginatedRateLimiter:
        limitForPeriod: 15
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      userBookingsRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      destinationBookingsRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      statusBookingsRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      deleteRateLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      createBookingRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      getBookingByIdRateLimiter:
        limitForPeriod: 15
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      travelDateRangeRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      paymentStatusPriceRangeRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      upcomingBookingsRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      countByStatusRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      softDeleteRateLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      bookingReferenceRateLimiter:
        limitForPeriod: 15
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      paymentStatusRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      confirmBookingRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
      advancedFilterRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
```

## Environment Variables
```properties
spring.application.name=${SPRING_APPLICATION_NAME}
deploy.env=${DEPLOY_ENV}
server.port=${SERVER_PORT}
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO}
spring.jpa.show-sql=${SPRING_JPA_SHOW_SQL}
spring.jpa.properties.hibernate.format_sql=${SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL}
```

## Setup Instructions
1. Clone the repository: `git clone https://github.com/rupal-draft/Tripezzy-Backend.git`
2. Navigate to the project folder: `cd booking-service`
3. Configure the `.env` file with required environment variables.
4. Build the project: `mvn clean install`
5. Run the application: `mvn spring-boot:run`

## Contributing
Feel free to submit pull requests or open issues to improve this microservice.


