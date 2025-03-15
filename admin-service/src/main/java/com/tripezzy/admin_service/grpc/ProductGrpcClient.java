package com.tripezzy.admin_service.grpc;

import com.tripezzy.product_service.grpc.Product;
import com.tripezzy.product_service.grpc.ProductRequest;
import com.tripezzy.product_service.grpc.ProductResponse;
import com.tripezzy.product_service.grpc.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductGrpcClient {
    private final ProductServiceGrpc.ProductServiceBlockingStub productStub;

    public ProductGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("eCommerce-service", 8001)
                .usePlaintext()
                .build();
        productStub = ProductServiceGrpc.newBlockingStub(channel);
    }

    @Cacheable(value = "products", key = "products + '-' + #page + '-' + #size")
    public List<Product> getAllProducts(int page, int size) {
        ProductRequest request = ProductRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();

        ProductResponse response = productStub.getAllProducts(request);
        return response.getProductsList();
    }
}
