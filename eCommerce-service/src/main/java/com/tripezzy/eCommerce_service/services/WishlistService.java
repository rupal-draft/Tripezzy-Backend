package com.tripezzy.eCommerce_service.services;

import com.tripezzy.eCommerce_service.dto.WishlistDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishlistService {
    WishlistDto addToWishlist(Long productId);
    Page<WishlistDto> getWishlistByUserId(Pageable pageable);
    void removeFromWishlist(Long wishlistId, Long productId);
}
