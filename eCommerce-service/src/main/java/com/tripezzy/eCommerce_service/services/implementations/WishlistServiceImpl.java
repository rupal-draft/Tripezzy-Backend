package com.tripezzy.eCommerce_service.services.implementations;

import com.tripezzy.eCommerce_service.auth.UserContext;
import com.tripezzy.eCommerce_service.auth.UserContextHolder;
import com.tripezzy.eCommerce_service.dto.ProductDto;
import com.tripezzy.eCommerce_service.dto.WishlistDto;
import com.tripezzy.eCommerce_service.entity.Product;
import com.tripezzy.eCommerce_service.entity.Wishlist;
import com.tripezzy.eCommerce_service.exceptions.IllegalState;
import com.tripezzy.eCommerce_service.exceptions.ResourceNotFound;
import com.tripezzy.eCommerce_service.repositories.ProductRepository;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {


    private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public WishlistServiceImpl(WishlistRepository wishlistRepository, ModelMapper modelMapper, ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.modelMapper = modelMapper;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public WishlistDto addToWishlist(Long productId) {
        log.info("Adding product to wishlist for ID: {}", productId);

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + productId));

        UserContext userContext = UserContextHolder.getUserDetails();
        Long userId = userContext.getUserId();

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUserId(userId);
                    return newWishlist;
                });

        if (wishlist.getProducts().contains(product)) {
            throw new IllegalState("Product is already in the wishlist");
        }

        wishlist.addProduct(product);
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        log.info("Product added to wishlist successfully with ID: {}", savedWishlist.getId());

        WishlistDto wishlistDto = new WishlistDto();
        wishlistDto.setUserId(savedWishlist.getUserId());

        List<ProductDto> productDtos = savedWishlist.getProducts().stream()
                .map(prod -> modelMapper.map(prod, ProductDto.class))
                .toList();
        wishlistDto.setProductDtos(productDtos);

        return wishlistDto;
    }

    @Override
    @Cacheable(value = "wishlist", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<WishlistDto> getWishlistByUserId(Pageable pageable) {
        UserContext userContext = UserContextHolder.getUserDetails();
        Long userId = userContext.getUserId();
        if (userId != userId) {
            throw new IllegalState("Unauthorized access");
        }
        log.info("Fetching wishlist for user ID: {}", userId);
        return wishlistRepository.findByUserId(userId, pageable)
                .map(wishlist -> {
                    WishlistDto wishlistDto = modelMapper.map(wishlist, WishlistDto.class);

                    List<ProductDto> productDtos = wishlist.getProducts().stream()
                            .map(product -> modelMapper.map(product, ProductDto.class))
                            .collect(Collectors.toList());

                    wishlistDto.setProductDtos(productDtos);

                    return wishlistDto;
                });
    }

    @Override
    @Transactional
    @CacheEvict(value = "wishlist", key = "#wishlistId")
    public void removeFromWishlist(Long wishlistId, Long productId) {
        log.info("Removing product with ID: {} from wishlist ID: {}", productId, wishlistId);

        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new ResourceNotFound("Wishlist not found with ID: " + wishlistId));

        boolean removed = wishlist.getProducts().removeIf(product -> product.getId().equals(productId));

        if (!removed) {
            throw new ResourceNotFound("Product not found in wishlist with ID: " + productId);
        }

        if (wishlist.getProducts().isEmpty()) {
            wishlistRepository.delete(wishlist);
            log.info("Wishlist deleted as it was empty, ID: {}", wishlistId);
        } else {
            wishlistRepository.save(wishlist);
            log.info("Product removed successfully from wishlist, ID: {}", wishlistId);
        }
    }

}
