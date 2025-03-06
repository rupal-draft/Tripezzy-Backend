package com.tripezzy.eCommerce_service.strategy;

import com.tripezzy.eCommerce_service.entity.CartItem;

public interface DiscountStrategy {
    double applyDiscount(CartItem cartItem);
}
