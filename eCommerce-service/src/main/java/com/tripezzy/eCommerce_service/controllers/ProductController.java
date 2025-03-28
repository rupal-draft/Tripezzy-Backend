package com.tripezzy.eCommerce_service.controllers;

import com.tripezzy.eCommerce_service.advices.ApiError;
import com.tripezzy.eCommerce_service.advices.ApiResponse;
import com.tripezzy.eCommerce_service.annotations.RoleRequired;
import com.tripezzy.eCommerce_service.dto.ProductDto;
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
    @RoleRequired("SELLER")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "createProductRateLimitFallback")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping(path = "/public/{productId}")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "getProductByIdRateLimitFallback")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping("/public")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "getAllProductsRateLimitFallback")
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @PutMapping("/{productId}")
    @RoleRequired("SELLER")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "updateProductRateLimitFallback")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.updateProduct(productId, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/public/filter")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "filterProductsRateLimitFallback")
    public ResponseEntity<Page<ProductDto>> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Pageable pageable) {
        return ResponseEntity.ok(productService.filterProducts(category, minPrice, maxPrice, pageable));
    }

    @DeleteMapping("/soft-delete/{productId}")
    @RoleRequired("SELLER")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "softDeleteProductRateLimitFallback")
    public ResponseEntity<ApiResponse<String>> softDeleteProduct(@PathVariable Long productId) {
        productService.softDeleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product soft deleted successfully"));
    }

    @GetMapping("/public/search")
    @RateLimiter(name = "productRateLimiter", fallbackMethod = "searchProductsRateLimitFallback")
    public ResponseEntity<Page<ProductDto>> searchProducts(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(query, pageable));
    }

    public ResponseEntity<ApiResponse<String>> rateLimitFallback(String serviceName, Throwable throwable) {
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .setMessage("Too many requests to " + serviceName + ". Please try again later.")
                .setStatus(HttpStatus.TOO_MANY_REQUESTS)
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(apiError));
    }

    public ResponseEntity<ApiResponse<String>> createProductRateLimitFallback(ProductDto productDto, Throwable throwable) {
        return rateLimitFallback("createProduct", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getProductByIdRateLimitFallback(Long productId, Throwable throwable) {
        return rateLimitFallback("getProductById", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getAllProductsRateLimitFallback(Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getAllProducts", throwable);
    }

    public ResponseEntity<ApiResponse<String>> updateProductRateLimitFallback(Long productId, ProductDto productDto, Throwable throwable) {
        return rateLimitFallback("updateProduct", throwable);
    }

    public ResponseEntity<ApiResponse<String>> filterProductsRateLimitFallback(String category, Double minPrice, Double maxPrice, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("filterProducts", throwable);
    }

    public ResponseEntity<ApiResponse<String>> softDeleteProductRateLimitFallback(Long productId, Throwable throwable) {
        return rateLimitFallback("softDeleteProduct", throwable);
    }

    public ResponseEntity<ApiResponse<String>> searchProductsRateLimitFallback(String query, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("searchProducts", throwable);
    }

}
