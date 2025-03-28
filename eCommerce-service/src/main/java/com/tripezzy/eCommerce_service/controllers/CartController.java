package com.tripezzy.eCommerce_service.controllers;

import com.tripezzy.eCommerce_service.advices.ApiError;
import com.tripezzy.eCommerce_service.advices.ApiResponse;
import com.tripezzy.eCommerce_service.auth.UserContext;
import com.tripezzy.eCommerce_service.auth.UserContextHolder;
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

    @PostMapping("/add/items")
    @RateLimiter(name = "cartRateLimiter", fallbackMethod = "addItemToCartRateLimitFallback")
    public ResponseEntity<CartDto> addItemToCart(
            @RequestBody CartItemDto cartItemDto) {
        UserContext userContext = UserContextHolder.getUserDetails();
        Long userId = userContext.getUserId();
        CartDto updatedCart = cartService.addItemToCart(userId, cartItemDto);
        return new ResponseEntity<>(updatedCart, HttpStatus.CREATED);
    }

    @GetMapping
    @RateLimiter(name = "cartRateLimiter", fallbackMethod = "getMyCartRateLimitFallback")
    public ResponseEntity<CartDto> getMyCart() {
        UserContext userContext = UserContextHolder.getUserDetails();
        Long userId = userContext.getUserId();
        CartDto cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/items/{productId}")
    @RateLimiter(name = "cartRateLimiter", fallbackMethod = "removeItemFromCartRateLimitFallback")
    public ResponseEntity<ApiResponse<String>> removeItemFromCart(
            @PathVariable Long productId) {
        UserContext userContext = UserContextHolder.getUserDetails();
        Long userId = userContext.getUserId();
        cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
    }

    @PutMapping("/total-cost/update")
    @RateLimiter(name = "cartRateLimiter", fallbackMethod = "calculateTotalCostRateLimitFallback")
    public ResponseEntity<ApiResponse<Double>> calculateTotalCost(
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) Double discountPercentage,
            @RequestParam(required = false) Integer minQuantity) {
        UserContext userContext = UserContextHolder.getUserDetails();
        Long userId = userContext.getUserId();
        double totalCost = cartService.calculateTotalCost(userId, discountType, discountPercentage, minQuantity);
        return ResponseEntity.ok(ApiResponse.success(totalCost));
    }

    public ResponseEntity<ApiResponse<String>> addItemToCartRateLimitFallback(CartItemDto cartItemDto, Throwable throwable) {
        return rateLimitFallback("addItemToCart", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getMyCartRateLimitFallback(Throwable throwable) {
        return rateLimitFallback("getMyCart", throwable);
    }

    public ResponseEntity<ApiResponse<String>> removeItemFromCartRateLimitFallback(Long productId, Throwable throwable) {
        return rateLimitFallback("removeItemFromCart", throwable);
    }

    public ResponseEntity<ApiResponse<String>> calculateTotalCostRateLimitFallback(
            String discountType, Double discountPercentage, Integer minQuantity, Throwable throwable) {
        return rateLimitFallback("calculateTotalCost", throwable);
    }

    private ResponseEntity<ApiResponse<String>> rateLimitFallback(String serviceName, Throwable throwable) {
        ApiError apiError = new ApiError
                .ApiErrorBuilder()
                .setMessage("Too many requests to " + serviceName + ". Please try again later.")
                .setStatus(HttpStatus.TOO_MANY_REQUESTS).build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(apiError));
    }

}
