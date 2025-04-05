package com.tripezzy.eCommerce_service.services.implementations;

import com.tripezzy.eCommerce_service.dto.ProductDto;
import com.tripezzy.eCommerce_service.entity.Product;
import com.tripezzy.eCommerce_service.exceptions.*;
import com.tripezzy.eCommerce_service.repositories.ProductRepository;
import com.tripezzy.eCommerce_service.services.ProductService;
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
        try {
            log.info("Creating a new product: {}", productDto.getName());

            // Validate input
            if (productDto == null) {
                throw new BadRequestException("Product data cannot be null");
            }
            if (productDto.getName() == null || productDto.getName().trim().isEmpty()) {
                throw new BadRequestException("Product name is required");
            }
            if (productDto.getPrice() == null || productDto.getPrice() <= 0) {
                throw new BadRequestException("Product price must be positive");
            }

            // Map and save product
            Product product = modelMapper.map(productDto, Product.class);

            Product savedProduct;
            try {
                savedProduct = productRepository.save(product);
            } catch (DataIntegrityViolationException e) {
                log.error("Data integrity violation while creating product: {}", productDto.getName(), e);
                throw new DataIntegrityViolation("Product creation failed due to data constraints");
            } catch (DataAccessException e) {
                log.error("Database error while creating product: {}", productDto.getName(), e);
                throw new ServiceUnavailable("Unable to create product. Please try again later.");
            }

            log.info("Product created successfully with ID: {}", savedProduct.getId());
            return modelMapper.map(savedProduct, ProductDto.class);

        } catch (RuntimeException e) {
            log.error("Unexpected error while creating product", e);
            if (e instanceof ResponseStatusException || e instanceof DataIntegrityViolationException) {
                throw e;
            }
            throw new IllegalState("An unexpected error occurred while creating the product");
        }
    }

    @Override
    @Cacheable(value = "product", key = "#productId")
    public ProductDto getProductById(Long productId) {
        try {
            log.info("Fetching product by ID: {}", productId);

            if (productId == null || productId <= 0) {
                throw new BadRequestException("Invalid product ID: " + productId);
            }

            Product product;
            try {
                product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + productId));
            } catch (DataAccessException e) {
                log.error("Database error while fetching product ID: {}", productId, e);
                throw new ServiceUnavailable("Unable to retrieve product. Please try again later.");
            }

            if (product.isDeleted()) {
                throw new ResourceNotFound("Product not found with ID: " + productId);
            }

            return modelMapper.map(product, ProductDto.class);

        } catch (RuntimeException e) {
            log.error("Unexpected error while fetching product ID: {}", productId, e);
            if (e instanceof ResponseStatusException || e instanceof DataAccessException) {
                throw e;
            }
            throw new IllegalState("An unexpected error occurred while fetching the product");
        }
    }

    @Override
    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        try {
            log.info("Fetching all products (paginated)");

            // Validate pagination
            if (pageable == null) {
                throw new BadRequestException("Pagination parameters are required");
            }

            Page<Product> productPage;
            try {
                productPage = productRepository.findAllByDeletedFalse(pageable);
            } catch (DataAccessException e) {
                log.error("Database error while fetching products", e);
                throw new ServiceUnavailable("Unable to retrieve products. Please try again later.");
            }

            return productPage.map(product -> modelMapper.map(product, ProductDto.class));

        } catch (RuntimeException e) {
            log.error("Unexpected error while fetching products", e);
            if (e instanceof ResponseStatusException || e instanceof DataAccessException) {
                throw e;
            }
            throw new IllegalState("An unexpected error occurred while fetching products");
        }
    }

    @Override
    @Cacheable(value = "filteredProducts", key = "#category + '-' + #minPrice + '-' + #maxPrice + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDto> filterProducts(String category, Double minPrice, Double maxPrice, Pageable pageable) {
        try {
            log.info("Filtering products with category: {}, minPrice: {}, maxPrice: {}", category, minPrice, maxPrice);

            // Validate inputs
            if (pageable == null) {
                throw new BadRequestException("Pagination parameters are required");
            }
            if (minPrice != null && minPrice < 0) {
                throw new BadRequestException("Minimum price cannot be negative");
            }
            if (maxPrice != null && maxPrice < 0) {
                throw new BadRequestException("Maximum price cannot be negative");
            }
            if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                throw new BadRequestException("Minimum price cannot be greater than maximum price");
            }

            Page<Product> filteredProducts;
            try {
                filteredProducts = productRepository.filterProducts(
                        category,
                        minPrice,
                        maxPrice,
                        pageable
                );
            } catch (DataAccessException e) {
                log.error("Database error while filtering products", e);
                throw new ServiceUnavailable("Unable to filter products. Please try again later.");
            }

            return filteredProducts.map(product -> modelMapper.map(product, ProductDto.class));

        } catch (RuntimeException e) {
            log.error("Unexpected error while filtering products", e);
            if (e instanceof ResponseStatusException || e instanceof DataAccessException) {
                throw e;
            }
            throw new IllegalState("An unexpected error occurred while filtering products");
        }
    }

    @Override
    @Cacheable(value = "searchedProducts", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        try {
            log.info("Searching products with query: {}", query);

            // Validate inputs
            if (pageable == null) {
                throw new BadRequestException("Pagination parameters are required");
            }
            if (query == null || query.trim().isEmpty()) {
                throw new BadRequestException("Search query cannot be empty");
            }

            Page<Product> searchResults;
            try {
                searchResults = productRepository.searchProducts(query, pageable);
            } catch (DataAccessException e) {
                log.error("Database error while searching products", e);
                throw new ServiceUnavailable("Unable to search products. Please try again later.");
            }

            return searchResults.map(product -> modelMapper.map(product, ProductDto.class));

        } catch (RuntimeException e) {
            log.error("Unexpected error while searching products", e);
            if (e instanceof ResponseStatusException || e instanceof DataAccessException) {
                throw e;
            }
            throw new IllegalState("An unexpected error occurred while searching products");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public ProductDto updateProduct(Long productId, ProductDto productDto) {
        try {
            log.info("Updating product with ID: {}", productId);

            // Validate inputs
            if (productId == null || productId <= 0) {
                throw new BadRequestException("Invalid product ID: " + productId);
            }
            if (productDto == null) {
                throw new BadRequestException("Product data cannot be null");
            }
            if (productDto.getName() == null || productDto.getName().trim().isEmpty()) {
                throw new BadRequestException("Product name is required");
            }
            if (productDto.getPrice() == null || productDto.getPrice() <= 0) {
                throw new BadRequestException("Product price must be positive");
            }

            Product product;
            try {
                product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + productId));
            } catch (DataAccessException e) {
                log.error("Database error while fetching product for update ID: {}", productId, e);
                throw new ServiceUnavailable("Unable to retrieve product for update. Please try again later.");
            }

            if (product.isDeleted()) {
                throw new ResourceNotFound("Product not found with ID: " + productId);
            }

            modelMapper.map(productDto, product);

            Product updatedProduct;
            try {
                updatedProduct = productRepository.save(product);
            } catch (DataIntegrityViolationException e) {
                log.error("Data integrity violation while updating product ID: {}", productId, e);
                throw new DataIntegrityViolation("Product update failed due to data constraints");
            } catch (DataAccessException e) {
                log.error("Database error while updating product ID: {}", productId, e);
                throw new ServiceUnavailable("Unable to update product. Please try again later.");
            }

            log.info("Product updated successfully with ID: {}", productId);
            return modelMapper.map(updatedProduct, ProductDto.class);

        } catch (RuntimeException e) {
            log.error("Unexpected error while updating product ID: {}", productId, e);
            if (e instanceof ResponseStatusException || e instanceof DataAccessException) {
                throw e;
            }
            throw new IllegalState("An unexpected error occurred while updating the product");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public void softDeleteProduct(Long productId) {
        try {
            log.info("Soft deleting product with ID: {}", productId);

            if (productId == null || productId <= 0) {
                throw new BadRequestException("Invalid product ID: " + productId);
            }

            Product product;
            try {
                product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFound("Product not found with ID: " + productId));
            } catch (DataAccessException e) {
                log.error("Database error while fetching product for deletion ID: {}", productId, e);
                throw new ServiceUnavailable("Unable to retrieve product for deletion. Please try again later.");
            }

            if (product.isDeleted()) {
                throw new ResourceNotFound("Product not found with ID: " + productId);
            }

            product.setDeleted(true);

            try {
                productRepository.save(product);
            } catch (DataAccessException e) {
                log.error("Database error while soft deleting product ID: {}", productId, e);
                throw new ServiceUnavailable("Unable to delete product. Please try again later.");
            }

            log.info("Product soft deleted successfully with ID: {}", productId);

        } catch (RuntimeException e) {
            log.error("Unexpected error while soft deleting product ID: {}", productId, e);
            if (e instanceof ResponseStatusException || e instanceof DataAccessException) {
                throw e;
            }
            throw new IllegalState("An unexpected error occurred while deleting the product");
        }
    }
}
