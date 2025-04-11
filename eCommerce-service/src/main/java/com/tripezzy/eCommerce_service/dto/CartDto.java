package com.tripezzy.eCommerce_service.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public class CartDto implements Serializable {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    private List<CartItemResponseDto> items;

    public CartDto() {
    }

    public CartDto(Long userId, List<CartItemResponseDto> items) {
        this.userId = userId;
        this.items = items;
    }

    public @NotNull(message = "User ID cannot be null") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID cannot be null") Long userId) {
        this.userId = userId;
    }

    public List<CartItemResponseDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponseDto> items) {
        this.items = items;
    }
}
