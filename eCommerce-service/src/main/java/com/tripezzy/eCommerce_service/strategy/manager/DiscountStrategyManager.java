package com.tripezzy.eCommerce_service.strategy.manager;

import com.tripezzy.eCommerce_service.strategy.DiscountStrategy;
import com.tripezzy.eCommerce_service.strategy.implementations.PriceBasedDiscountStrategy;
import com.tripezzy.eCommerce_service.strategy.implementations.QuantityBasedDiscountStrategy;
import org.springframework.stereotype.Component;

@Component
public class DiscountStrategyManager {

    public DiscountStrategy getDiscountStrategy(String discountType, Double discountPercentage, Integer minQuantity) {
        if ("price".equalsIgnoreCase(discountType)) {
            return new PriceBasedDiscountStrategy(discountPercentage);
        } else if ("quantity".equalsIgnoreCase(discountType)) {
            return new QuantityBasedDiscountStrategy( discountPercentage,minQuantity);
        }
        return item -> item.getProduct().getPrice();
    }
}
