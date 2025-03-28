package com.tripezzy.eCommerce_service.controllers;

import com.tripezzy.eCommerce_service.advices.ApiError;
import com.tripezzy.eCommerce_service.advices.ApiResponse;
import com.tripezzy.eCommerce_service.dto.WishlistDto;
import com.tripezzy.eCommerce_service.services.WishlistService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{productId}")
    @RateLimiter(name = "wishlistRateLimiter", fallbackMethod = "addToWishlistRateLimitFallback")
    public ResponseEntity<WishlistDto> addToWishlist(@PathVariable Long productId) {
        WishlistDto savedWishlist = wishlistService.addToWishlist(productId);
        return new ResponseEntity<>(savedWishlist, HttpStatus.CREATED);
    }

    @GetMapping
    @RateLimiter(name = "wishlistRateLimiter", fallbackMethod = "getMyWishlistRateLimitFallback")
    public ResponseEntity<Page<WishlistDto>> getMyWishlist(Pageable pageable) {
        Page<WishlistDto> wishlist = wishlistService.getWishlistByUserId(pageable);
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/{wishlistId}")
    @RateLimiter(name = "wishlistRateLimiter", fallbackMethod = "removeFromWishlistRateLimitFallback")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(@PathVariable Long wishlistId, @RequestParam Long productId) {
        wishlistService.removeFromWishlist(wishlistId, productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist"));
    }

    public ResponseEntity<ApiResponse<String>> addToWishlistRateLimitFallback(Long productId, Throwable throwable) {
        return rateLimitFallback("addToWishlist", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getMyWishlistRateLimitFallback(Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getMyWishlist", throwable);
    }

    public ResponseEntity<ApiResponse<String>> removeFromWishlistRateLimitFallback(Long wishlistId, Long productId, Throwable throwable) {
        return rateLimitFallback("removeFromWishlist", throwable);
    }

    private ResponseEntity<ApiResponse<String>> rateLimitFallback(String serviceName, Throwable throwable) {
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .setMessage("Too many requests to " + serviceName + ". Please try again later.")
                .setStatus(HttpStatus.TOO_MANY_REQUESTS)
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(apiError));
    }
}
