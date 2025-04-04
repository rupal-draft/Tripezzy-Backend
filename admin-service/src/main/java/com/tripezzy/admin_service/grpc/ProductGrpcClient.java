package com.tripezzy.admin_service.grpc;

import com.tripezzy.admin_service.dto.ProductResponseDto;
import com.tripezzy.admin_service.exceptions.*;
import com.tripezzy.product_service.grpc.Product;
import com.tripezzy.product_service.grpc.ProductRequest;
import com.tripezzy.product_service.grpc.ProductResponse;
import com.tripezzy.product_service.grpc.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductGrpcClient {


    private static final Logger log = LoggerFactory.getLogger(ProductGrpcClient.class);
    private final ProductServiceGrpc.ProductServiceBlockingStub productStub;
    private final ManagedChannel channel;


    public ProductGrpcClient() {
        try{
            this.channel = ManagedChannelBuilder
                    .forAddress("localhost", 8001)
                    .usePlaintext()
                    .build();
            productStub = ProductServiceGrpc.newBlockingStub(channel);
            checkServiceHealth();
        } catch (Exception e) {
            log.error("Failed to initialize gRPC product client", e);
            throw new ServiceUnavailable("Product service is currently unavailable");
        }

    }

    private void checkServiceHealth() {
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(
                    HealthCheckRequest.newBuilder().build());

            if (response.getStatus() != HealthCheckResponse.ServingStatus.SERVING) {
                log.error("Product service is not healthy: {}", response.getStatus());
                throw new ServiceUnavailable("Product service is not healthy");
            }
            log.info("Product service health status: {}", response.getStatus());
        } catch (StatusRuntimeException e) {
            log.error("Product service health check failed", e);
            throw new ServiceUnavailable("Product service is unreachable");
        }
    }

    @Cacheable(value = "products", key = "'products-' + #page + '-' + #size")
    public List<ProductResponseDto> getAllProducts(int page, int size) {
        validatePaginationParams(page, size);
        log.info("Fetching all products - page: {}, size: {}", page, size);
        try{
            ProductRequest request = ProductRequest.newBuilder()
                    .setPage(page)
                    .setSize(size)
                    .build();

            ProductResponse response = productStub.getAllProducts(request);

            if(response == null || response.getProductsList().isEmpty()){
                log.warn("No products found for page {} and size {}", page, size);
                return Collections.emptyList();
            }
            return mapProductResponse(response);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get products");
            return Collections.emptyList();
        }

    }

    private List<ProductResponseDto> mapProductResponse(ProductResponse response) {
        return response.getProductsList().stream()
                .map(this::mapProduct)
                .collect(Collectors.toUnmodifiableList());
    }

    private ProductResponseDto mapProduct(Product product) {
        try {
            return new ProductResponseDto(
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStock(),
                    product.getCategory(),
                    product.getImageUrl()
            );
        } catch (Exception e) {
            log.error("Failed to map product data", e);
            throw new IllegalState("Failed to process product data");
        }
    }
    
    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }

    private void handleGrpcException(StatusRuntimeException e, String context) {
        Status.Code code = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        log.error("gRPC error [{}] {}: {}", code, context, description, e);

        switch (code) {
            case NOT_FOUND:
                throw new ResourceNotFound(description != null ? description : "Requested Product not found");
            case INVALID_ARGUMENT:
                throw new BadRequestException(description != null ? description : "Invalid Product request parameters");
            case PERMISSION_DENIED:
                throw new AccessForbidden(description != null ? description : "Product permission denied");
            case UNAVAILABLE:
                throw new ServiceUnavailable("Product service is currently unavailable");
            case FAILED_PRECONDITION:
                throw new IllegalState(description != null ? description : "Invalid Product state");
            default:
                throw new ServiceUnavailable("Failed to process Product request");
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            log.warn("Failed to shutdown gRPC channel properly", e);
            Thread.currentThread().interrupt();
        }
    }
}
