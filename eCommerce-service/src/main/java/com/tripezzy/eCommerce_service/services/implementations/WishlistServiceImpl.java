package com.tripezzy.eCommerce_service.services.implementations;

import com.tripezzy.eCommerce_service.dto.WishlistDto;
import com.tripezzy.eCommerce_service.entity.Wishlist;
import com.tripezzy.eCommerce_service.exceptions.ResourceNotFound;
import com.tripezzy.eCommerce_service.repositories.WishlistRepository;
import com.tripezzy.eCommerce_service.services.WishlistService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistServiceImpl implements WishlistService {


    private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);
    private final WishlistRepository wishlistRepository;
    private final ModelMapper modelMapper;

    public WishlistServiceImpl(WishlistRepository wishlistRepository, ModelMapper modelMapper) {
        this.wishlistRepository = wishlistRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public WishlistDto addToWishlist(WishlistDto wishlistDto) {
        log.info("Adding product to wishlist for user ID: {}", wishlistDto.getUserId());
        Wishlist wishlist = modelMapper.map(wishlistDto, Wishlist.class);
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        log.info("Product added to wishlist successfully with ID: {}", savedWishlist.getId());
        return modelMapper.map(savedWishlist, WishlistDto.class);
    }

    @Override
    @Cacheable(value = "wishlist", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<WishlistDto> getWishlistByUserId(Long userId, Pageable pageable) {
        log.info("Fetching wishlist for user ID: {}", userId);
        return wishlistRepository.findByUserId(userId, pageable)
                .map(wishlist -> modelMapper.map(wishlist, WishlistDto.class));
    }

    @Override
    @Transactional
    @CacheEvict(value = "wishlist", key = "#wishlistId")
    public void removeFromWishlist(Long wishlistId) {
        log.info("Removing wishlist item with ID: {}", wishlistId);
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new ResourceNotFound("Wishlist item not found with ID: " + wishlistId));
        wishlistRepository.delete(wishlist);
        log.info("Wishlist item removed successfully with ID: {}", wishlistId);
    }
}
