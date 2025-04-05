package com.tripezzy.eCommerce_service.grpc;

import com.tripezzy.eCommerce_service.dto.ProductDto;
import com.tripezzy.eCommerce_service.exceptions.ResourceNotFound;
import com.tripezzy.eCommerce_service.exceptions.ServiceUnavailable;
import com.tripezzy.eCommerce_service.services.ProductService;
import com.tripezzy.product_service.grpc.Product;
import com.tripezzy.product_service.grpc.ProductRequest;
import com.tripezzy.product_service.grpc.ProductResponse;
import com.tripezzy.product_service.grpc.ProductServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {


    private static final Logger log = LoggerFactory.getLogger(ProductGrpcService.class);
    private final ProductService productService;

    public ProductGrpcService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void getAllProducts(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for getAllProducts, page: {}, size: {}", request.getPage(), request.getSize());

            if (request.getPage() < 0) {
                throw new IllegalArgumentException("Page number cannot be negative");
            }
            if (request.getSize() <= 0 || request.getSize() > 100) {
                throw new IllegalArgumentException("Page size must be between 1 and 100");
            }

            Page<ProductDto> productPage = productService.getAllProducts(
                    PageRequest.of(request.getPage(), request.getSize())
            );

            List<Product> grpcProducts = productPage.getContent().stream()
                    .map(productDto -> Product.newBuilder()
                            .setName(productDto.getName())
                            .setDescription(productDto.getDescription())
                            .setPrice(productDto.getPrice())
                            .setStock(productDto.getStock())
                            .setCategory(productDto.getCategory())
                            .setImageUrl(productDto.getImageUrl())
                            .build()
                    )
                    .collect(Collectors.toList());

            ProductResponse response = ProductResponse.newBuilder()
                    .addAllProducts(grpcProducts)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully processed getAllProducts request");

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (ResourceNotFound e) {
            log.warn("Resource not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (ServiceUnavailable e) {
            log.error("Service unavailable: {}", e.getMessage());
            responseObserver.onError(Status.UNAVAILABLE
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in getAllProducts", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
