package com.tripezzy.eCommerce_service.services;

import com.tripezzy.eCommerce_service.dto.CartDto;
import com.tripezzy.eCommerce_service.dto.CartItemDto;
import com.tripezzy.eCommerce_service.dto.CartPaymentDto;

public interface CartService {
    CartDto addItemToCart(Long userId, CartItemDto cartItemDto);
    CartDto getCartByUserId(Long userId);
    void removeItemFromCart(Long userId, Long productId);
    double calculateTotalCost(Long userId, String discountType, Double discountPercentage, Integer minQuantity);
    CartPaymentDto getPaymentDetails(Long cartId);
}
