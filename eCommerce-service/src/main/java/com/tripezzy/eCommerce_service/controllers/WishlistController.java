package com.tripezzy.eCommerce_service.controllers;

import com.tripezzy.eCommerce_service.dto.WishlistDto;
import com.tripezzy.eCommerce_service.exceptions.RuntimeConflict;
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

    @PostMapping
    @RateLimiter(name = "wishlistRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<WishlistDto> addToWishlist(@RequestBody WishlistDto wishlistDto) {
        WishlistDto savedWishlist = wishlistService.addToWishlist(wishlistDto);
        return new ResponseEntity<>(savedWishlist, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    @RateLimiter(name = "wishlistRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<WishlistDto>> getWishlistByUserId(@PathVariable Long userId, Pageable pageable) {
        Page<WishlistDto> wishlist = wishlistService.getWishlistByUserId(userId, pageable);
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/{wishlistId}")
    @RateLimiter(name = "wishlistRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long wishlistId) {
        wishlistService.removeFromWishlist(wishlistId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<String> rateLimitFallback(Long blogId, RuntimeConflict e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again later.");
    }
}
