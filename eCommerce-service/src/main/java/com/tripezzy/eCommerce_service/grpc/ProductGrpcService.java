package com.tripezzy.eCommerce_service.grpc;

import com.tripezzy.eCommerce_service.dto.ProductDto;
import com.tripezzy.eCommerce_service.services.ProductService;
import com.tripezzy.product_service.grpc.Product;
import com.tripezzy.product_service.grpc.ProductRequest;
import com.tripezzy.product_service.grpc.ProductResponse;
import com.tripezzy.product_service.grpc.ProductServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService productService;

    public ProductGrpcService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void getAllProducts(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        Page<ProductDto> productPage = productService.getAllProducts(PageRequest.of(request.getPage(), request.getSize()));

        List<Product> grpcProducts = productPage.getContent().stream().map(productDto ->
                Product.newBuilder()
                        .setName(productDto.getName())
                        .setDescription(productDto.getDescription())
                        .setPrice(productDto.getPrice())
                        .setStock(productDto.getStock())
                        .setCategory(productDto.getCategory())
                        .setImageUrl(productDto.getImageUrl())
                        .build()
        ).collect(Collectors.toList());

        ProductResponse response = ProductResponse.newBuilder()
                .addAllProducts(grpcProducts)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
