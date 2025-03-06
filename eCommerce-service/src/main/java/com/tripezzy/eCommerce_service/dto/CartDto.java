package com.tripezzy.eCommerce_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CartDto {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    private List<CartItemDto> items;

    public CartDto(Long userId, List<CartItemDto> items) {
        this.userId = userId;
        this.items = items;
    }

    public @NotNull(message = "User ID cannot be null") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID cannot be null") Long userId) {
        this.userId = userId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }
}
