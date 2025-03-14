package com.tripezzy.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PublicEndpointConfig {

    private final Set<String> publicEndpoints = Set.of(
            "/api/v1/users/auth/login",
            "/api/v1/users/auth/register",
            "/api/v1/admin/tours/public",
            "/api/v1/admin/destinations/public",
            "/api/v1/bookings/core/public",
            "/api/v1/shop/products/public",
            "/api/v1/blogs/core/public",
            "/api/v1/payments/core/public"
    );

    public boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream().anyMatch(path::startsWith);
    }
}
