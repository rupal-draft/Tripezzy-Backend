package com.tripezzy.eCommerce_service.strategy.implementations;

import com.tripezzy.eCommerce_service.entity.CartItem;
import com.tripezzy.eCommerce_service.strategy.DiscountStrategy;

public class QuantityBasedDiscountStrategy implements DiscountStrategy {

    private final double discountPercentage;
    private final int quantityThreshold;

    public QuantityBasedDiscountStrategy(double discountPercentage, int quantityThreshold) {
        this.discountPercentage = discountPercentage;
        this.quantityThreshold = quantityThreshold;
    }

    @Override
    public double applyDiscount(CartItem cartItem) {

        int quantity = cartItem
                .getQuantity();
        double price = cartItem
                .getProduct()
                .getPrice();

        if(quantity >= quantityThreshold){
            return price * (1 - discountPercentage / 100);
        }

        return price;
    }
}
