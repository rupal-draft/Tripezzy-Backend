package com.tripezzy.eCommerce_service.controllers;

import com.tripezzy.eCommerce_service.dto.ProductDto;
import com.tripezzy.eCommerce_service.exceptions.RuntimeConflict;
import com.tripezzy.eCommerce_service.services.ProductService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping(path = "/{productId}")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @PutMapping("/{productId}")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.updateProduct(productId, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<ProductDto>> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Pageable pageable) {
        return ResponseEntity.ok(productService.filterProducts(category, minPrice, maxPrice, pageable));
    }

    @DeleteMapping("/soft-delete/{productId}")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long productId) {
        productService.softDeleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<ProductDto>> searchProducts(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(query, pageable));
    }

    public ResponseEntity<String> rateLimitFallback(Long blogId, RuntimeConflict e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again later.");
    }
}
