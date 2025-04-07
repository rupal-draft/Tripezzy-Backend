package com.tripezzy.payment_service.grpc.client;

import com.tripezzy.eCommerce_service.grpc.CartPaymentResponse;
import com.tripezzy.eCommerce_service.grpc.CartRequest;
import com.tripezzy.eCommerce_service.grpc.CartServiceGrpc;
import com.tripezzy.payment_service.dto.CartPaymentResponseDto;
import com.tripezzy.payment_service.exceptions.ResourceNotFound;
import com.tripezzy.payment_service.exceptions.ServiceUnavailable;
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
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CartGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(CartGrpcClient.class);
    private final CartServiceGrpc.CartServiceBlockingStub cartStub;
    private final ManagedChannel channel;

    public CartGrpcClient() {
        this.channel = ManagedChannelBuilder
                .forAddress("localhost", 8001)
                .usePlaintext()
                .build();
        this.cartStub = CartServiceGrpc.newBlockingStub(channel);
    }

    private void checkServiceHealth() {
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());
            if (response.getStatus() != HealthCheckResponse.ServingStatus.SERVING) {
                log.error("Cart service is not healthy: {}", response.getStatus());
                throw new ServiceUnavailable("Cart service is not healthy");
            }
            log.info("Cart service health status: {}", response.getStatus());
        } catch (StatusRuntimeException e) {
            log.error("Cart service health check failed", e);
            throw new ServiceUnavailable("Cart service is unreachable");
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

    public CartPaymentResponseDto getPaymentDetails(Long cartId) {
        checkServiceHealth();
        try {
            log.info("Fetching cart payment details for cart ID: {}", cartId);

            if (cartId == null || cartId <= 0) {
                throw new IllegalArgumentException("Invalid cart ID");
            }

            CartRequest request = CartRequest.newBuilder()
                    .setCartId(cartId)
                    .build();

            CartPaymentResponse cartPaymentResponse = cartStub.getPaymentDetails(request);

            return new CartPaymentResponseDto(
                    cartPaymentResponse.getAmount(),
                    cartPaymentResponse.getQuantity(),
                    cartPaymentResponse.getName(),
                    cartPaymentResponse.getCurrency()
            );
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get cart payment details");
            throw new ServiceUnavailable("Unable to retrieve cart payment at this time");
        }
    }

    private void handleGrpcException(StatusRuntimeException e, String context) {
        Status.Code code = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        log.error("gRPC error [{}] {}: {}", code, context, description, e);

        switch (code) {
            case NOT_FOUND:
                throw new ResourceNotFound(description != null ? description : "Cart not found");
            case INVALID_ARGUMENT:
                throw new IllegalArgumentException(description != null ? description : "Invalid request parameters");
            case UNAVAILABLE:
                throw new ServiceUnavailable("Cart service is currently unavailable");
            default:
                throw new ServiceUnavailable("Failed to process cart request");
        }
    }
}
