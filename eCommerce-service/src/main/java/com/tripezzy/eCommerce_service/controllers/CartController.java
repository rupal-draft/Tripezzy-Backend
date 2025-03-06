package com.tripezzy.eCommerce_service.controllers;

import com.tripezzy.eCommerce_service.dto.CartDto;
import com.tripezzy.eCommerce_service.dto.CartItemDto;
import com.tripezzy.eCommerce_service.services.CartService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{userId}/items")
    @RateLimiter(name = "cartRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<CartDto> addItemToCart(
            @PathVariable Long userId,
            @RequestBody CartItemDto cartItemDto) {
        CartDto updatedCart = cartService.addItemToCart(userId, cartItemDto);
        return new ResponseEntity<>(updatedCart, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    @RateLimiter(name = "cartRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<CartDto> getCartByUserId(@PathVariable Long userId) {
        CartDto cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    @RateLimiter(name = "cartRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/total-cost")
    @RateLimiter(name = "cartRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Double> calculateTotalCost(
            @PathVariable Long userId,
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) Double discountPercentage,
            @RequestParam(required = false) Integer minQuantity) {
        double totalCost = cartService.calculateTotalCost(userId, discountType, discountPercentage, minQuantity);
        return ResponseEntity.ok(totalCost);
    }
}
