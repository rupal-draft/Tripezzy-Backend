package com.tripezzy.eCommerce_service.services.implementations;

import com.tripezzy.eCommerce_service.auth.UserContext;
import com.tripezzy.eCommerce_service.auth.UserContextHolder;
import com.tripezzy.eCommerce_service.dto.ProductDto;
import com.tripezzy.eCommerce_service.dto.WishlistDto;
import com.tripezzy.eCommerce_service.entity.Product;
import com.tripezzy.eCommerce_service.entity.Wishlist;
import com.tripezzy.eCommerce_service.exceptions.*;
import com.tripezzy.eCommerce_service.repositories.ProductRepository;
import com.tripezzy.eCommerce_service.repositories.WishlistRepository;
import com.tripezzy.eCommerce_service.services.WishlistService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               ModelMapper modelMapper,
                               ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.modelMapper = modelMapper;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public WishlistDto addToWishlist(Long productId) {
        try {
            log.info("Adding product to wishlist for ID: {}", productId);

            if (productId == null || productId <= 0) {
                throw new BadRequestException("Invalid product ID: " + productId);
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null) {
                throw new AccessForbidden("User authentication required");
            }

            Long userId = userContext.getUserId();
            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID in context");
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + productId));

            Wishlist wishlist;
            try {
                wishlist = wishlistRepository.findByUserId(userId)
                        .orElseGet(() -> {
                            Wishlist newWishlist = new Wishlist();
                            newWishlist.setUserId(userId);
                            return newWishlist;
                        });
            } catch (DataAccessException e) {
                log.error("Database error while fetching wishlist for user ID: {}", userId, e);
                throw new ServiceUnavailable("Unable to access wishlist data. Please try again later.");
            }

            if (wishlist.getProducts().contains(product)) {
                throw new RuntimeConflict("Product is already in the wishlist");
            }

            wishlist.addProduct(product);

            Wishlist savedWishlist;
            try {
                savedWishlist = wishlistRepository.save(wishlist);
            } catch (DataIntegrityViolationException e) {
                log.error("Data integrity violation while saving wishlist for user ID: {}", userId, e);
                throw new DataIntegrityViolation("Failed to update wishlist due to data constraints");
            } catch (DataAccessException e) {
                log.error("Database error while saving wishlist for user ID: {}", userId, e);
                throw new ServiceUnavailable("Unable to save wishlist data. Please try again later.");
            }

            log.info("Product added to wishlist successfully with ID: {}", savedWishlist.getId());

            // Map to DTO
            WishlistDto wishlistDto = new WishlistDto();
            wishlistDto.setUserId(savedWishlist.getUserId());

            List<ProductDto> productDtos = savedWishlist.getProducts().stream()
                    .map(prod -> modelMapper.map(prod, ProductDto.class))
                    .toList();
            wishlistDto.setProductDtos(productDtos);

            return wishlistDto;

        } catch (RuntimeException e) {
            log.error("Unexpected error while adding to wishlist for product ID: {}", productId, e);
            if (e instanceof ResponseStatusException || e instanceof DataIntegrityViolationException) {
                throw e;
            }
            throw new IllegalState("An unexpected error occurred while processing your request");
        }
    }

    @Override
    @Cacheable(value = "wishlist", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<WishlistDto> getWishlistByUserId(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new BadRequestException("Pagination parameters are required");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null) {
                throw new AccessForbidden("User authentication required");
            }

            Long userId = userContext.getUserId();
            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID in context");
            }

            log.info("Fetching wishlist for user ID: {}", userId);

            Page<Wishlist> wishlistPage;
            try {
                wishlistPage = wishlistRepository.findByUserId(userId, pageable);
            } catch (DataAccessException e) {
                log.error("Database error while fetching wishlist for user ID: {}", userId, e);
                throw new ServiceUnavailable("Unable to retrieve wishlist data. Please try again later.");
            }

            return wishlistPage.map(wishlist -> {
                WishlistDto wishlistDto = modelMapper.map(wishlist, WishlistDto.class);

                List<ProductDto> productDtos = wishlist.getProducts().stream()
                        .map(product -> modelMapper.map(product, ProductDto.class))
                        .collect(Collectors.toList());

                wishlistDto.setProductDtos(productDtos);

                return wishlistDto;
            });

        } catch (RuntimeException e) {
            log.error("Unexpected error while fetching wishlist", e);
            if (e instanceof ResponseStatusException || e instanceof DataAccessException) {
                throw e; // Re-throw already handled exceptions
            }
            throw new IllegalState("An unexpected error occurred while processing your request");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "wishlist", key = "#wishlistId")
    public void removeFromWishlist(Long wishlistId, Long productId) {
        try {
            log.info("Removing product with ID: {} from wishlist ID: {}", productId, wishlistId);

            // Validate inputs
            if (wishlistId == null || wishlistId <= 0) {
                throw new BadRequestException("Invalid wishlist ID: " + wishlistId);
            }
            if (productId == null || productId <= 0) {
                throw new BadRequestException("Invalid product ID: " + productId);
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null) {
                throw new AccessForbidden("User authentication required");
            }

            Long userId = userContext.getUserId();
            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID in context");
            }

            Wishlist wishlist;
            try {
                wishlist = wishlistRepository.findById(wishlistId)
                        .orElseThrow(() -> new ResourceNotFound("Wishlist not found with ID: " + wishlistId));
            } catch (DataAccessException e) {
                log.error("Database error while fetching wishlist ID: {}", wishlistId, e);
                throw new ServiceUnavailable("Unable to access wishlist data. Please try again later.");
            }

            if (!wishlist.getUserId().equals(userId)) {
                throw new AccessForbidden("You are not authorized to modify this wishlist");
            }

            boolean removed = wishlist.getProducts().removeIf(product -> product.getId().equals(productId));

            if (!removed) {
                throw new ResourceNotFound("Product not found in wishlist with ID: " + productId);
            }

            try {
                if (wishlist.getProducts().isEmpty()) {
                    wishlistRepository.delete(wishlist);
                    log.info("Wishlist deleted as it was empty, ID: {}", wishlistId);
                } else {
                    wishlistRepository.save(wishlist);
                    log.info("Product removed successfully from wishlist, ID: {}", wishlistId);
                }
            } catch (DataAccessException e) {
                log.error("Database error while updating wishlist ID: {}", wishlistId, e);
                throw new ServiceUnavailable("Unable to update wishlist. Please try again later.");
            }

        } catch (RuntimeException e) {
            log.error("Unexpected error while removing from wishlist ID: {}, product ID: {}", wishlistId, productId, e);
            if (e instanceof ResponseStatusException || e instanceof DataAccessException) {
                throw e;
            }
            throw new IllegalState("An unexpected error occurred while processing your request");
        }
    }
}
