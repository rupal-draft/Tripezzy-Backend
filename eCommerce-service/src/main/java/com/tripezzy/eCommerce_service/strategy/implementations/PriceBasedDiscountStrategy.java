package com.tripezzy.eCommerce_service.strategy.implementations;

import com.tripezzy.eCommerce_service.entity.CartItem;
import com.tripezzy.eCommerce_service.strategy.DiscountStrategy;

public class PriceBasedDiscountStrategy implements DiscountStrategy {

    private final double discountPercentage;

    public PriceBasedDiscountStrategy(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double applyDiscount(CartItem cartItem) {
        double price = cartItem
                .getProduct()
                .getPrice();
        return price * (1 - discountPercentage / 100);
    }
}
