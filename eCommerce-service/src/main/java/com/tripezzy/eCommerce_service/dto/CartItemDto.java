package com.tripezzy.eCommerce_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CartItemDto {
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    public CartItemDto() {
    }

    public CartItemDto(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public @NotNull(message = "Product ID cannot be null") Long getProductId() {
        return productId;
    }

    public void setProductId(@NotNull(message = "Product ID cannot be null") Long productId) {
        this.productId = productId;
    }

    public @NotNull(message = "Quantity cannot be null") @Positive(message = "Quantity must be positive") Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(@NotNull(message = "Quantity cannot be null") @Positive(message = "Quantity must be positive") Integer quantity) {
        this.quantity = quantity;
    }
}
