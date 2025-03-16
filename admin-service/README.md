# Admin Service Microservice

This repository contains the code for an Admin Service microservice, built using Spring Boot. This service provides functionalities for managing destinations, tour packages, and aggregating data from other services via gRPC.

## Features

* **Destination Management:**
    * Create, update, delete, and soft delete destinations.
    * Retrieve, search, and filter destinations.
* **Tour Package Management:**
    * Create, update, delete, and soft delete tour packages.
    * Retrieve, search, and filter tour packages.
    * Retrieve tour packages by destination.
* **gRPC Aggregation:**
    * Retrieve blog posts from the Blog Service.
    * Retrieve booking information from the Booking Service.
    * Retrieve product information from the Product Service.
    * Retrieve payment information from the Payment Service.
* **Role-Based Access Control (RBAC):**
    * Restricted access to administrative endpoints using custom `@RoleRequired("ADMIN")` annotation.
* **Rate Limiting:**
    * Implemented rate limiting using Resilience4j to prevent abuse.
* **Database:**
    * Uses a relational database (configured via environment variables).
* **Auditing:**
    * Inherits auditing from Auditable Class, managing created and updated timestamps.

## Technologies Used

* Spring Boot
* Spring Data JPA
* Resilience4j (for rate limiting)
* Hibernate
* Validation API
* gRPC
* Protobuf

## Entities and Database Schemas

The service uses the following entities, which translate to database schemas:

### `Destination`

| Column        | Data Type        | Constraints             | Description                                                                  |
|---------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the destination.                                     |
| `name`        | `VARCHAR(100)`   | `NOT NULL`, `UNIQUE`, `INDEX` | Name of the destination.                                                    |
| `country`     | `VARCHAR(50)`    | `NOT NULL`, `INDEX`     | Country of the destination.                                                 |
| `description` | `VARCHAR(1000)`  | `NOT NULL`              | Description of the destination.                                             |
| `deleted`     | `BOOLEAN`        | `NOT NULL`              | Soft delete flag.                                                               |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the destination was created.                                 |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the destination was last updated.                             |

### `TourPackage`

| Column          | Data Type        | Constraints             | Description                                                                  |
|-----------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`            | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the tour package.                                  |
| `name`          | `VARCHAR(100)`   | `NOT NULL`, `UNIQUE`, `INDEX` | Name of the tour package.                                                  |
| `description`   | `VARCHAR(1000)`  | `NOT NULL`              | Description of the tour package.                                              |
| `price`         | `DECIMAL`        | `NOT NULL`, `POSITIVE`, `INDEX` | Price of the tour package.                                                 |
| `capacity`      | `INTEGER`        | `NOT NULL`, `MIN(1)`    | Maximum capacity of the tour package.                                        |
| `startDate`     | `TIMESTAMP`      | `NOT NULL`, `FUTURE_OR_PRESENT` | Start date of the tour package.                                             |
| `endDate`       | `TIMESTAMP`      | `NOT NULL`, `FUTURE`    | End date of the tour package.                                               |
| `destination_id`| `BIGINT`         | `NOT NULL`, `FOREIGN KEY`, `INDEX` | ID of the destination associated with the tour package.                  |
| `deleted`       | `BOOLEAN`        | `NOT NULL`              | Soft delete flag.                                                               |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the tour package was created.                                 |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the tour package was last updated.                             |

## API Endpoints

### Destination Controller

| Method | Endpoint                    | Description                                                                                                                                                                                              |
|--------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/destinations`             | Creates a new destination.                                                                                                                                                                              |
| `GET`  | `/destinations`             | Retrieves all destinations (paginated).                                                                                                                                                                 |
| `GET`  | `/destinations/{id}`        | Retrieves a destination by its ID.                                                                                                                                                                     |
| `PUT`  | `/destinations/{id}`        | Updates a destination.                                                                                                                                                                                  |
| `DELETE`| `/destinations/{id}`        | Deletes a destination.                                                                                                                                                                                  |
| `DELETE`| `/destinations/soft-delete/{id}` | Soft deletes a destination.                                                                                                                                                                     |
| `GET`  | `/destinations/search`      | Searches destinations based on a keyword.                                                                                                                                                              |
| `GET`  | `/destinations/filter`      | Filters destinations based on country, category, minimum price, and maximum price.                                                                                                                         |

### Tour Controller

| Method | Endpoint                    | Description                                                                                                                                                                                              |
|--------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/tours`                    | Creates a new tour package.                                                                                                                                                                             |
| `GET`  | `/tours`                    | Retrieves all tour packages (paginated).                                                                                                                                                                |
| `GET`  | `/tours/{id}`               | Retrieves a tour package by its ID.                                                                                                                                                                    |
| `GET`  | `/tours/destination/{destinationId}` | Retrieves tour packages by destination ID.                                                                                                                                                     |
| `PUT`  | `/tours/{id}`               | Updates a tour package.                                                                                                                                                                                 |
| `DELETE`| `/tours/{id}`               | Deletes a tour package.                                                                                                                                                                                 |
| `DELETE`| `/tours/soft-delete/{id}`   | Soft deletes a tour package.                                                                                                                                                                            |
| `GET`  | `/tours/search`             | Searches tour packages based on a keyword.                                                                                                                                                              |
| `GET`  | `/tours/filter`             | Filters tour packages based on destination ID, minimum price, maximum price, category, and status.                                                                                                        |

### Admin Controller (gRPC Aggregation)

| Method | Endpoint                          | Description                                                                                                                                                                                          |
|--------|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GET`  | `/core/blogs`                    | Retrieves all blog posts from the Blog Service (paginated).                                                                                                                                          |
| `GET`  | `/core/bookings/paginated`       | Retrieves all bookings from the Booking Service (paginated).                                                                                                                                      |
| `GET`  | `/core/bookings/user/{userId}`   | Retrieves bookings by user ID from the Booking Service (paginated).                                                                                                                                |
| `GET`  | `/core/bookings/destination/{destinationId}` | Retrieves bookings by destination ID from the Booking Service (paginated).                                                                                                                |
| `GET`  | `/core/bookings/travel-date-range` | Retrieves bookings by travel date range from the Booking Service (paginated).                                                                                                                      |
| `DELETE`| `/core/soft-delete/{bookingId}`   | Soft deletes a booking in the Booking Service.                                                                                                                                                    |
| `GET`  | `/core/payment-status/{paymentStatus}` | Retrieves bookings by payment status from the Booking Service.                                                                                                                                     |
| `PATCH`| `/core/{bookingId}/confirm`        | Confirms a booking in the Booking Service.                                                                                                                                                          |
| `GET`  | `/core/products`                 | Retrieves all products from the Product Service (paginated).                                                                                                                                       |
| `GET`  | `/core/payments`                 | Retrieves all payments from the Payment Service.                                                                                                                                                     |
| `GET`  | `/core/payments/{userId}`         | Retrieves all payments by user ID from the Payment Service.                                                                                                                                         |

## Configuration

The service is configured using `application.properties` or `application.yml` (environment variable based).

## Getting Started

1.  **Clone the repository:**

    ```bash
    git clone [repository URL]# Admin Service Microservice

This repository contains the code for an Admin Service microservice, built using Spring Boot. This service provides functionalities for managing destinations, tour packages, and aggregating data from other services via gRPC.

## Features

* **Destination Management:**
    * Create, update, delete, and soft delete destinations.
    * Retrieve, search, and filter destinations.
* **Tour Package Management:**
    * Create, update, delete, and soft delete tour packages.
    * Retrieve, search, and filter tour packages.
    * Retrieve tour packages by destination.
* **gRPC Aggregation:**
    * Retrieve blog posts from the Blog Service.
    * Retrieve booking information from the Booking Service.
    * Retrieve product information from the Product Service.
    * Retrieve payment information from the Payment Service.
* **Role-Based Access Control (RBAC):**
    * Restricted access to administrative endpoints using custom `@RoleRequired("ADMIN")` annotation.
* **Rate Limiting:**
    * Implemented rate limiting using Resilience4j to prevent abuse.
* **Database:**
    * Uses a relational database (configured via environment variables).
* **Auditing:**
    * Inherits auditing from Auditable Class, managing created and updated timestamps.

## Technologies Used

* Spring Boot
* Spring Data JPA
* Resilience4j (for rate limiting)
* Lombok
* Hibernate
* Validation API
* gRPC
* Protobuf

## Entities and Database Schemas

The service uses the following entities, which translate to database schemas:

### `Destination`

| Column        | Data Type        | Constraints             | Description                                                                  |
|---------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the destination.                                     |
| `name`        | `VARCHAR(100)`   | `NOT NULL`, `UNIQUE`, `INDEX` | Name of the destination.                                                    |
| `country`     | `VARCHAR(50)`    | `NOT NULL`, `INDEX`     | Country of the destination.                                                 |
| `description` | `VARCHAR(1000)`  | `NOT NULL`              | Description of the destination.                                             |
| `deleted`     | `BOOLEAN`        | `NOT NULL`              | Soft delete flag.                                                               |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the destination was created.                                 |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the destination was last updated.                             |

### `TourPackage`

| Column          | Data Type        | Constraints             | Description                                                                  |
|-----------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`            | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the tour package.                                  |
| `name`          | `VARCHAR(100)`   | `NOT NULL`, `UNIQUE`, `INDEX` | Name of the tour package.                                                  |
| `description`   | `VARCHAR(1000)`  | `NOT NULL`              | Description of the tour package.                                              |
| `price`         | `DECIMAL`        | `NOT NULL`, `POSITIVE`, `INDEX` | Price of the tour package.                                                 |
| `capacity`      | `INTEGER`        | `NOT NULL`, `MIN(1)`    | Maximum capacity of the tour package.                                        |
| `startDate`     | `TIMESTAMP`      | `NOT NULL`, `FUTURE_OR_PRESENT` | Start date of the tour package.                                             |
| `endDate`       | `TIMESTAMP`      | `NOT NULL`, `FUTURE`    | End date of the tour package.                                               |
| `destination_id`| `BIGINT`         | `NOT NULL`, `FOREIGN KEY`, `INDEX` | ID of the destination associated with the tour package.                  |
| `deleted`       | `BOOLEAN`        | `NOT NULL`              | Soft delete flag.                                                               |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the tour package was created.                                 |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the tour package was last updated.                             |

## API Endpoints

### Destination Controller

| Method | Endpoint                    | Description                                                                                                                                                                                              |
|--------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/destinations`             | Creates a new destination.                                                                                                                                                                              |
| `GET`  | `/destinations`             | Retrieves all destinations (paginated).                                                                                                                                                                 |
| `GET`  | `/destinations/{id}`        | Retrieves a destination by its ID.                                                                                                                                                                     |
| `PUT`  | `/destinations/{id}`        | Updates a destination.                                                                                                                                                                                  |
| `DELETE`| `/destinations/{id}`        | Deletes a destination.                                                                                                                                                                                  |
| `DELETE`| `/destinations/soft-delete/{id}` | Soft deletes a destination.                                                                                                                                                                     |
| `GET`  | `/destinations/search`      | Searches destinations based on a keyword.                                                                                                                                                              |
| `GET`  | `/destinations/filter`      | Filters destinations based on country, category, minimum price, and maximum price.                                                                                                                         |

### Tour Controller

| Method | Endpoint                    | Description                                                                                                                                                                                              |
|--------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/tours`                    | Creates a new tour package.                                                                                                                                                                             |
| `GET`  | `/tours`                    | Retrieves all tour packages (paginated).                                                                                                                                                                |
| `GET`  | `/tours/{id}`               | Retrieves a tour package by its ID.                                                                                                                                                                    |
| `GET`  | `/tours/destination/{destinationId}` | Retrieves tour packages by destination ID.                                                                                                                                                     |
| `PUT`  | `/tours/{id}`               | Updates a tour package.                                                                                                                                                                                 |
| `DELETE`| `/tours/{id}`               | Deletes a tour package.                                                                                                                                                                                 |
| `DELETE`| `/tours/soft-delete/{id}`   | Soft deletes a tour package.                                                                                                                                                                            |
| `GET`  | `/tours/search`             | Searches tour packages based on a keyword.                                                                                                                                                              |
| `GET`  | `/tours/filter`             | Filters tour packages based on destination ID, minimum price, maximum price, category, and status.                                                                                                        |

### Admin Controller (gRPC Aggregation)

| Method | Endpoint                          | Description                                                                                                                                                                                          |
|--------|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GET`  | `/core/blogs`                    | Retrieves all blog posts from the Blog Service (paginated).                                                                                                                                          |
| `GET`  | `/core/bookings/paginated`       | Retrieves all bookings from the Booking Service (paginated).                                                                                                                                      |
| `GET`  | `/core/bookings/user/{userId}`   | Retrieves bookings by user ID from the Booking Service (paginated).                                                                                                                                |
| `GET`  | `/core/bookings/destination/{destinationId}` | Retrieves bookings by destination ID from the Booking Service (paginated).                                                                                                                |
| `GET`  | `/core/bookings/travel-date-range` | Retrieves bookings by travel date range from the Booking Service (paginated).                                                                                                                      |
| `DELETE`| `/core/soft-delete/{bookingId}`   | Soft deletes a booking in the Booking Service.                                                                                                                                                    |
| `GET`  | `/core/payment-status/{paymentStatus}` | Retrieves bookings by payment status from the Booking Service.                                                                                                                                     |
| `PATCH`| `/core/{bookingId}/confirm`        | Confirms a booking in the Booking Service.                                                                                                                                                          |
| `GET`  | `/core/products`                 | Retrieves all products from the Product Service (paginated).                                                                                                                                       |
| `GET`  | `/core/payments`                 | Retrieves all payments from the Payment Service.                                                                                                                                                     |
| `GET`  | `/core/payments/{userId}`         | Retrieves all payments by user ID from the Payment Service.                                                                                                                                         |

## Configuration

The service is configured using `application.properties` or `application.yml` (environment variable based).

## Rate Limiter configuration.

The Rate limiter is set with these parameters.
```yaml
resilience4j:
  ratelimiter:
    instances:
      createDestinationRateLimiter:
        limitForPeriod: 10 # Number of requests allowed per period
        limitRefreshPeriod: 10s # Period in seconds for refreshing limits
        timeoutDuration: 1s # Timeout for acquiring a permission
      getAllDestinationsRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      getDestinationByIdRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      updateDestinationRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      deleteDestinationRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      softDeleteDestinationRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      searchDestinationsRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      filterDestinationsRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      createTourRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      getAllToursRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      getTourByIdRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      getToursByDestinationRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      updateTourRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      deleteTourRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      softDeleteTourRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      searchToursRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      filterToursRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
```