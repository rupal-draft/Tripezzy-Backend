package com.tripezzy.api_gateway.config;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PublicEndpointConfig {

    private final Set<String> publicEndpoints = Set.of(
            "/users/auth/login",
            "/users/auth/register",
            "/admin/tours/public",
            "/admin/destinations/public",
            "/bookings/core/public",
            "/shop/products/public",
            "/blogs/core/public",
            "/payments/core/public"
    );

    public boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream().anyMatch(path::startsWith);
    }
}
