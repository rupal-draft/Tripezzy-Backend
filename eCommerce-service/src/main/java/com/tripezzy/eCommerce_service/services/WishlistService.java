package com.tripezzy.eCommerce_service.services;

import com.tripezzy.eCommerce_service.dto.WishlistDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishlistService {
    WishlistDto addToWishlist(WishlistDto wishlistDto);
    Page<WishlistDto> getWishlistByUserId(Long userId, Pageable pageable);
    void removeFromWishlist(Long wishlistId);
}
