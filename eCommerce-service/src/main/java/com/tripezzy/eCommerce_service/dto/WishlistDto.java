package com.tripezzy.eCommerce_service.dto;

import jakarta.validation.constraints.NotNull;

public class WishlistDto {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    public WishlistDto(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public @NotNull(message = "User ID cannot be null") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID cannot be null") Long userId) {
        this.userId = userId;
    }

    public @NotNull(message = "Product ID cannot be null") Long getProductId() {
        return productId;
    }

    public void setProductId(@NotNull(message = "Product ID cannot be null") Long productId) {
        this.productId = productId;
    }
}
