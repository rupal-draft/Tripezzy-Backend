package com.tripezzy.eCommerce_service.repositories;

import com.tripezzy.eCommerce_service.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Page<Wishlist> findByUserId(Long userId, Pageable pageable);

    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);
}
