package com.tripezzy.payment_service.grpc.client;

import com.tripezzy.eCommerce_service.grpc.CartPaymentResponse;
import com.tripezzy.eCommerce_service.grpc.CartRequest;
import com.tripezzy.eCommerce_service.grpc.CartServiceGrpc;
import com.tripezzy.payment_service.dto.CartPaymentResponseDto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CartGrpcClient {


    private static final Logger log = LoggerFactory.getLogger(CartGrpcClient.class);
    private final CartServiceGrpc.CartServiceBlockingStub cartStub;

    public CartGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 8001)
                .usePlaintext()
                .build();
        cartStub = CartServiceGrpc.newBlockingStub(channel);
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());
            log.info("Health Status: " + response.getStatus());
        } catch (Exception e) {
            log.error("Blog service is unreachable. Proceeding without it.");
        }
    }

    public CartPaymentResponseDto getPaymentDetails(Long cartId) {
        CartRequest request = CartRequest.newBuilder()
                .setCartId(cartId)
                .build();

        CartPaymentResponse cartPaymentResponse = cartStub.getPaymentDetails(request);

        return new CartPaymentResponseDto(cartPaymentResponse.getAmount(),
                cartPaymentResponse.getQuantity(),
                cartPaymentResponse.getName(),
                cartPaymentResponse.getCurrency()
        );
    }
}
