# API Gateway Service Documentation

This document describes the API Gateway service, which acts as a single entry point for all client requests to the Tripezzy platform's microservices. It handles request routing, authentication, and other cross-cutting concerns.

## Features

* **Request Routing:** Routes incoming requests to the appropriate microservice based on the request path.

* **Load Balancing:** Leverages Spring Cloud LoadBalancer (through Eureka) to distribute traffic across multiple instances of each microservice.

* **Authentication:** Includes an `AuthenticationFilter` to validate JWT tokens in incoming requests, securing the microservices.

* **Centralized Configuration:** Configured using Spring Cloud Gateway's routing mechanism, simplifying microservice management.

## Technologies Used

* Spring Boot

* Spring Cloud Gateway

* Spring Cloud LoadBalancer (Eureka)

* JSON Web Tokens (JWT)

## Configuration

The API Gateway is configured using `application.properties` or `application.yml` (environment variable based).

**Example `application.yml` configuration:**

```yaml
spring:
  application:
    name: api-gateway # Service name
  eureka:
    client:
      service-url:
        defaultZone: http://eureka-server:8761/eureka # URL of the Eureka server
  server:
    port: 8080 # Port on which the gateway listens

jwt:
  secretKey: your-secret-key # Secret key used to validate JWT tokens

spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE # Service ID in Eureka
          predicates:
            - Path=/api/v1/users/** # Route requests starting with /api/v1/users
          filters:
            - StripPrefix=2      # Remove the /api/v1 prefix before forwarding
            - name: AuthenticationFilter # Apply the AuthenticationFilter

        - id: admin-service
          uri: lb://ADMIN-SERVICE
          predicates:
            - Path=/api/v1/admin/**
          filters:
            - StripPrefix=2
            - name: AuthenticationFilter

        - id: booking-service
          uri: lb://BOOKING-SERVICE
          predicates:
            - Path=/api/v1/bookings/**
          filters:
            - StripPrefix=2
            - name: AuthenticationFilter

        - id: eCommerce-service
          uri: lb://ECOMMERCE-SERVICE
          predicates:
            - Path=/api/v1/shop/**
          filters:
            - StripPrefix=2
            - name: AuthenticationFilter

        - id: blog-service
          uri: lb://BLOG-SERVICE
          predicates:
            - Path=/api/v1/blogs/**
          filters:
            - StripPrefix=2
            - name: AuthenticationFilter

        - id: payment-service
          uri: lb://PAYMENT-SERVICE
          predicates:
            - Path=/api/v1/payments/**
          filters:
            - StripPrefix=2
            - name: AuthenticationFilter

        - id: notification-service
          uri: lb://NOTIFICATION-SERVICE
          predicates:
            - Path=/api/v1/notifications/**
          filters:
            - StripPrefix=2
            - name: AuthenticationFilter

        - id: uploader-service
          uri: lb://UPLOADER-SERVICE
          predicates:
            - Path=/api/v1/uploader/**
          filters:
            - StripPrefix=2
            - name: AuthenticationFilter
```

## Route Definitions

The `spring.cloud.gateway.routes` section defines how incoming requests are routed to different microservices. Each route includes:

* `id`: A unique identifier for the route.

* `uri`: The URI of the target microservice (using `lb://` for load balancing with Eureka).

* `predicates`: Conditions that must be met for a request to be matched to this route (e.g., `Path`).

* `filters`: Modifications to the request before it's forwarded to the microservice (e.g., `StripPrefix`, `AuthenticationFilter`).

## Authentication Filter

The `AuthenticationFilter` is a crucial component that intercepts incoming requests and validates the JWT token present in the `Authorization` header. It's applied to most routes to secure the microservices. The filter should:

1.  Extract the JWT token from the `Authorization` header.

2.  Validate the token's signature and claims using the configured `jwt.secretKey`.

3.  If the token is valid, allow the request to proceed.

4.  If the token is invalid or missing, reject the request with an appropriate error response (e.g., `401 Unauthorized`).