package com.tripezzy.eCommerce_service.services.implementations;

import com.tripezzy.eCommerce_service.dto.*;
import com.tripezzy.eCommerce_service.entity.Cart;
import com.tripezzy.eCommerce_service.entity.CartItem;
import com.tripezzy.eCommerce_service.entity.Product;
import com.tripezzy.eCommerce_service.exceptions.ResourceNotFound;
import com.tripezzy.eCommerce_service.repositories.CartRepository;
import com.tripezzy.eCommerce_service.repositories.ProductRepository;
import com.tripezzy.eCommerce_service.services.CartService;
import com.tripezzy.eCommerce_service.strategy.DiscountStrategy;
import com.tripezzy.eCommerce_service.strategy.manager.DiscountStrategyManager;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository, ModelMapper modelMapper, DiscountStrategyManager discountStrategyManager) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.discountStrategyManager = discountStrategyManager;
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public CartDto addItemToCart(Long userId, CartItemDto cartItemDto) {
        log.info("Adding item to cart for user ID: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        Product product = productRepository
                .findById(cartItemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + cartItemDto.getProductId()));

        Optional<CartItem> existingItem = cart
                .getItems()
                .stream()
                .filter(item -> item
                        .getProduct()
                        .getId()
                        .equals(cartItemDto.getProductId()))
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
    }

    @Override
    @Cacheable(value = "cart", key = "#userId")
    public CartDto getCartByUserId(Long userId) {
        log.info("Fetching cart for user ID: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFound("Cart not found for user ID: " + userId));

        List<CartItemResponseDto> itemDtos = cart
                .getItems()
                .stream()
                .map(item ->
                        new CartItemResponseDto(modelMapper
                                .map(item.getProduct(), ProductDto.class), item.getQuantity()))
                .collect(Collectors.toList());

        return new CartDto(cart.getUserId(), itemDtos);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public void removeItemFromCart(Long userId, Long productId) {
        log.info("Removing item from cart for user ID: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFound("Cart not found for user ID: " + userId));

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        cartRepository.save(cart);
        log.info("Item removed from cart successfully for user ID: {}", userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public double calculateTotalCost(Long userId, String discountType, Double discountPercentage, Integer minQuantity) {
        log.info("Calculating total cost for user ID: {}", userId);
        Cart cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFound("Cart not found for user ID: " + userId));

        DiscountStrategy discountStrategy = discountStrategyManager.getDiscountStrategy(discountType, discountPercentage, minQuantity);

        double totalCost = cart
                .getItems()
                .stream()
                .mapToDouble(item -> discountStrategy.applyDiscount(item) * item.getQuantity())
                .sum();

        cart.setTotalAmount(totalCost);
        cartRepository.save(cart);

        log.info("Total cost calculated successfully for user ID: {}", userId);
        return totalCost;
    }

    @Override
    @Transactional
    public CartPaymentDto getPaymentDetails(Long cartId) {
        log.info("Fetching cart for payment for cart ID: {}", cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFound("Cart not found for user ID: " + cartId));

        CartPaymentDto cartPaymentDto = new CartPaymentDto();
        cartPaymentDto.setQuantity(cart
                .getItems()
                .stream()
                .mapToLong(CartItem::getQuantity)
                .sum());
        cartPaymentDto.setAmount(cart.getTotalAmount());
        cartPaymentDto.setName(
                cart.getItems()
                        .stream()
                        .map(cartItem -> cartItem.getProduct().getName())
                        .collect(Collectors.joining(" + "))
        );
        cartPaymentDto.setCurrency("USD");

        return cartPaymentDto;
    }
}
