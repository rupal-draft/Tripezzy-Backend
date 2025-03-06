package com.tripezzy.eCommerce_service.services;

import com.tripezzy.eCommerce_service.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductDto createProduct(ProductDto productDto);
    ProductDto getProductById(Long productId);
    Page<ProductDto> getAllProducts(Pageable pageable);
    Page<ProductDto> filterProducts(String category, Double minPrice, Double maxPrice, Pageable pageable);
    Page<ProductDto> searchProducts(String query, Pageable pageable);
    ProductDto updateProduct(Long productId, ProductDto productDto);
    void deleteProduct(Long productId);
    void softDeleteProduct(Long productId);
}
