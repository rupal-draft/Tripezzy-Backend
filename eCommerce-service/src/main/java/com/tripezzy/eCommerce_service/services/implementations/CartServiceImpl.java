package com.tripezzy.eCommerce_service.services.implementations;

import com.tripezzy.eCommerce_service.dto.*;
import com.tripezzy.eCommerce_service.entity.Cart;
import com.tripezzy.eCommerce_service.entity.CartItem;
import com.tripezzy.eCommerce_service.entity.Product;
import com.tripezzy.eCommerce_service.exceptions.*;
import com.tripezzy.eCommerce_service.repositories.CartRepository;
import com.tripezzy.eCommerce_service.repositories.ProductRepository;
import com.tripezzy.eCommerce_service.services.CartService;
import com.tripezzy.eCommerce_service.strategy.DiscountStrategy;
import com.tripezzy.eCommerce_service.strategy.manager.DiscountStrategyManager;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final DiscountStrategyManager discountStrategyManager;

    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository,
                           ModelMapper modelMapper, DiscountStrategyManager discountStrategyManager) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.discountStrategyManager = discountStrategyManager;
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public CartDto addItemToCart(Long userId, CartItemDto cartItemDto) {
        try {
            log.info("Adding item to cart for user ID: {}", userId);

            // Validate input
            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID");
            }
            if (cartItemDto == null || cartItemDto.getProductId() == null || cartItemDto.getQuantity() <= 0) {
                throw new BadRequestException("Invalid cart item data");
            }

            Cart cart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUserId(userId);
                        newCart.setItems(new ArrayList<>());
                        return cartRepository.save(newCart);
                    });

            Product product = productRepository.findById(cartItemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + cartItemDto.getProductId()));

            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(cartItemDto.getProductId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + cartItemDto.getQuantity());
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(cartItemDto.getQuantity());
                cart.getItems().add(newItem);
            }

            Cart updatedCart = cartRepository.save(cart);
            log.info("Item added to cart successfully for user ID: {}", userId);
            return modelMapper.map(updatedCart, CartDto.class);

        } catch (DataAccessException e) {
            log.error("Database error while adding item to cart for user ID: {}", userId, e);
            throw new DataIntegrityViolation("Failed to update cart due to database error");
        } catch (MappingException e) {
            log.error("Mapping error while adding item to cart", e);
            throw new IllegalState("Failed to process cart data");
        }
    }

    @Override
    @Cacheable(value = "cart", key = "#userId")
    public CartDto getCartByUserId(Long userId) {
        try {
            log.info("Fetching cart for user ID: {}", userId);

            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID");
            }

            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFound("Cart not found for user ID: " + userId));

            List<CartItemResponseDto> itemDtos = cart.getItems().stream()
                    .map(item -> {
                        try {
                            return new CartItemResponseDto(
                                    modelMapper.map(item.getProduct(), ProductDto.class),
                                    item.getQuantity()
                            );
                        } catch (MappingException e) {
                            log.error("Mapping error for product ID: {}", item.getProduct().getId(), e);
                            throw new IllegalState("Failed to map product data");
                        }
                    })
                    .collect(Collectors.toList());

            return new CartDto(cart.getUserId(), itemDtos);

        } catch (DataAccessException e) {
            log.error("Database error while fetching cart for user ID: {}", userId, e);
            throw new ServiceUnavailable("Unable to retrieve cart at this time");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public void removeItemFromCart(Long userId, Long productId) {
        try {
            log.info("Removing item from cart for user ID: {}", userId);

            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID");
            }
            if (productId == null || productId <= 0) {
                throw new BadRequestException("Invalid product ID");
            }

            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFound("Cart not found for user ID: " + userId));

            boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

            if (!removed) {
                throw new ResourceNotFound("Product not found in cart");
            }

            cartRepository.save(cart);
            log.info("Item removed from cart successfully for user ID: {}", userId);

        } catch (DataAccessException e) {
            log.error("Database error while removing item from cart for user ID: {}", userId, e);
            throw new DataIntegrityViolation("Failed to update cart due to database error");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public double calculateTotalCost(Long userId, String discountType, Double discountPercentage, Integer minQuantity) {
        try {
            log.info("Calculating total cost for user ID: {}", userId);

            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID");
            }
            if (discountType == null || discountType.isBlank()) {
                throw new BadRequestException("Discount type is required");
            }

            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFound("Cart not found for user ID: " + userId));

            DiscountStrategy discountStrategy = discountStrategyManager
                    .getDiscountStrategy(discountType, discountPercentage, minQuantity);

            double totalCost = cart.getItems().stream()
                    .mapToDouble(item -> discountStrategy.applyDiscount(item) * item.getQuantity())
                    .sum();

            cart.setTotalAmount(totalCost);
            cartRepository.save(cart);

            log.info("Total cost calculated successfully for user ID: {}", userId);
            return totalCost;

        } catch (DataAccessException e) {
            log.error("Database error while calculating total cost for user ID: {}", userId, e);
            throw new DataIntegrityViolation("Failed to update cart due to database error");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid discount parameters: {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public CartPaymentDto getPaymentDetails(Long cartId) {
        try {
            log.info("Fetching cart for payment for cart ID: {}", cartId);

            if (cartId == null || cartId <= 0) {
                throw new BadRequestException("Invalid cart ID");
            }

            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFound("Cart not found with ID: " + cartId));

            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                throw new IllegalState("Cannot process payment for empty cart");
            }

            CartPaymentDto cartPaymentDto = new CartPaymentDto();
            cartPaymentDto.setQuantity(cart.getItems().stream()
                    .mapToLong(CartItem::getQuantity)
                    .sum());
            cartPaymentDto.setAmount(cart.getTotalAmount());
            cartPaymentDto.setName(cart.getItems().stream()
                    .map(cartItem -> cartItem.getProduct().getName())
                    .collect(Collectors.joining(" + ")));
            cartPaymentDto.setCurrency("USD");

            return cartPaymentDto;

        } catch (DataAccessException e) {
            log.error("Database error while fetching payment details for cart ID: {}", cartId, e);
            throw new ServiceUnavailable("Unable to retrieve payment details at this time");
        }
    }
}
