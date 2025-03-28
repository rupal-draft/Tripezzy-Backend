package com.tripezzy.eCommerce_service.services.implementations;

import com.tripezzy.eCommerce_service.dto.ProductDto;
import com.tripezzy.eCommerce_service.entity.Product;
import com.tripezzy.eCommerce_service.exceptions.ResourceNotFound;
import com.tripezzy.eCommerce_service.repositories.ProductRepository;
import com.tripezzy.eCommerce_service.services.ProductService;
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
public class ProductServiceImpl implements ProductService {


    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductServiceImpl(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        log.info("Creating a new product: {}", productDto.getName());
        Product product = modelMapper.map(productDto, Product.class);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return modelMapper.map(savedProduct, ProductDto.class);
    }

    @Override
    @Cacheable(value = "product", key = "#productId")
    public ProductDto getProductById(Long productId) {
        log.info("Fetching product by ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + productId));
        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        log.info("Fetching all products (paginated)");
        return productRepository.findAll(pageable)
                .map(product -> modelMapper.map(product, ProductDto.class));
    }

    @Override
    @Cacheable(value = "filteredProducts", key = "#category + '-' + #minPrice + '-' + #maxPrice + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDto> filterProducts(String category, Double minPrice, Double maxPrice, Pageable pageable) {
        log.info("Filtering products with category: {}, minPrice: {}, maxPrice: {}", category, minPrice, maxPrice);
        return productRepository.filterProducts(category, minPrice, maxPrice, pageable)
                .map(product -> modelMapper.map(product, ProductDto.class));
    }

    @Override
    @Cacheable(value = "searchedProducts", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        log.info("Searching products with query: {}", query);
        return productRepository.searchProducts(query, pageable)
                .map(product -> modelMapper.map(product, ProductDto.class));
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public ProductDto updateProduct(Long productId, ProductDto productDto) {
        log.info("Updating product with ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + productId));
        modelMapper.map(productDto, product);
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", productId);
        return modelMapper.map(updatedProduct, ProductDto.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public void softDeleteProduct(Long productId) {
        log.info("Soft deleting product with ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + productId));
        product.setDeleted(true);
        productRepository.save(product);
        log.info("Product soft deleted successfully with ID: {}", productId);
    }
}
