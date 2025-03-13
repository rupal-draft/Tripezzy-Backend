# User Service 

This directory contains the code for a User Service microservice, built using Spring Boot. This service provides functionalities for user registration, login, and profile management for sellers and guides.

## Features

* **User Management:**
    * User registration (signup).
    * User login with JWT authentication.
    * Seller profile onboarding.
    * Guide profile onboarding.
    * User role management (user, seller, guide).
    * Soft delete functionality.
* **Authentication and Authorization:**
    * JWT (JSON Web Token) based authentication.
    * Password hashing using BCrypt.
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
* JWT (JSON Web Tokens)
* BCrypt

## Entities and Database Schemas

The service uses the following entities, which translate to database schemas:

### `User`

| Column        | Data Type        | Constraints             | Description                                                                  |
|---------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the user.                                           |
| `firstName`   | `VARCHAR(50)`    | `NOT NULL`              | First name of the user.                                                      |
| `lastName`    | `VARCHAR(50)`    | `NOT NULL`              | Last name of the user.                                                       |
| `email`       | `VARCHAR(100)`   | `NOT NULL`, `UNIQUE`, `INDEX` | Email address of the user.                                                   |
| `phoneNumber` | `VARCHAR(15)`   | `NOT NULL`, `UNIQUE`      | Phone number of the user.                                                    |
| `password`    | `VARCHAR(255)`   | `NOT NULL`              | Hashed password of the user.                                                 |
| `role`        | `VARCHAR(20)`    | `NOT NULL`, `INDEX`     | Role of the user (e.g., `USER`, `SELLER`, `GUIDE`).                          |
| `isDeleted`   | `BOOLEAN`        | `DEFAULT FALSE`         | Soft delete flag.                                                               |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the user was created.                                       |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the user was last updated.                                   |

### `Seller`

| Column              | Data Type        | Constraints             | Description                                                                  |
|---------------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`                | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the seller profile.                                |
| `businessName`      | `VARCHAR(255)`   | `NOT NULL`              | Name of the seller's business.                                               |
| `businessDescription` | `TEXT`           | `NOT NULL`              | Description of the seller's business.                                        |
| `businessAddress`   | `VARCHAR(255)`   | `NOT NULL`              | Address of the seller's business.                                            |
| `contactNumber`     | `VARCHAR(15)`   | `NOT NULL`, `UNIQUE`      | Contact phone number for the seller's business.                               |
| `user_id`           | `BIGINT`         | `NOT NULL`, `FOREIGN KEY`, `INDEX` | ID of the associated user.                                              |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the seller profile was created.                                       |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the seller profile was last updated.                                   |

### `Guide`

| Column          | Data Type        | Constraints             | Description                                                                  |
|-----------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`            | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the guide profile.                                 |
| `languagesSpoken` | `VARCHAR(255)`   | `NOT NULL`              | Languages spoken by the guide.                                               |
| `experience`    | `VARCHAR(1000)`  | `NOT NULL`              | Experience details of the guide.                                             |
| `user_id`       | `BIGINT`         | `NOT NULL`, `FOREIGN KEY`, `INDEX` | ID of the associated user.                                              |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the guide profile was created.                                       |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the guide profile was last updated.                                   |

## API Endpoints

| Method | Endpoint              | Description                                                                                                                                                                                              |
|--------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/auth/signup`        | Registers a new user.                                                                                                                                                                                    |
| `POST` | `/auth/login`         | Authenticates a user and returns a JWT.                                                                                                                                                                 |
| `POST` | `/auth/onboard/seller`| Onboards a user as a seller.                                                                                                                                                                             |
| `POST` | `/auth/onboard/guide` | Onboards a user as a guide.                                                                                                                                                                              |

## Configuration

The service is configured using `application.properties` or `application.yml` (environment variable based).

## Dependencies

* **JWT Dependencies:**

    ```xml
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.6</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.6</version>
    </dependency>
    ```

* **BCrypt Dependency:**

    ```xml
    <dependency>
        <groupId>org.mindrot</groupId>
        <artifactId>jbcrypt</artifactId>
        <version>0.4</version>
    </dependency>
    ```

## Configure environment variables:**
    * Set the database credentials and other configuration properties in your environment.

## Rate Limiter configuration.

The Rate limiter is set with these parameters.
```yaml
resilience4j:
  ratelimiter:
    instances:
      signupLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 1m
        timeoutDuration: 0
      loginLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1m
        timeoutDuration: 0
      sellerOnboardLimiter:
        limitForPeriod: 3
        limitRefreshPeriod: 1h
        timeoutDuration: 0
      guideOnboardLimiter:
        limitForPeriod: 3
        limitRefreshPeriod: 1h
        timeoutDuration: 0
```
