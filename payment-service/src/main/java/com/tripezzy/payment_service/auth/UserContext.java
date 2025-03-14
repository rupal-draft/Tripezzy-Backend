package com.tripezzy.payment_service.auth;

public class UserContext {
    private Long userId;
    private String role;

    public UserContext(Long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
