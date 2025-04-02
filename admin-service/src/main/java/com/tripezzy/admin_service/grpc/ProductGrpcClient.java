package com.tripezzy.admin_service.grpc;

import com.tripezzy.admin_service.dto.ProductResponseDto;
import com.tripezzy.product_service.grpc.Product;
import com.tripezzy.product_service.grpc.ProductRequest;
import com.tripezzy.product_service.grpc.ProductResponse;
import com.tripezzy.product_service.grpc.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(ProductGrpcClient.class);
    private final ProductServiceGrpc.ProductServiceBlockingStub productStub;


    public ProductGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 8001)
                .usePlaintext()
                .build();
        productStub = ProductServiceGrpc.newBlockingStub(channel);
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());
            log.info("Health Status: " + response.getStatus());
        } catch (Exception e) {
            log.error("Blog service is unreachable. Proceeding without it.");
        }
    }

    @Cacheable(value = "products", key = "'products-' + #page + '-' + #size")
    public List<ProductResponseDto> getAllProducts(int page, int size) {
        ProductRequest request = ProductRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();

        ProductResponse response = productStub.getAllProducts(request);
        return response.getProductsList().stream()
                .map(product -> new ProductResponseDto(
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStock(),
                        product.getCategory(),
                        product.getImageUrl()
                ))
                .collect(Collectors.toList());
    }
}
