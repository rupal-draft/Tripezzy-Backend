package com.tripezzy.payment_service.grpc.client;

import com.tripezzy.eCommerce_service.grpc.CartPaymentResponse;
import com.tripezzy.eCommerce_service.grpc.CartRequest;
import com.tripezzy.eCommerce_service.grpc.CartServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

@Service
public class CartGrpcClient {
    private final CartServiceGrpc.CartServiceBlockingStub cartStub;

    public CartGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("eCommerce-service", 8001)
                .usePlaintext()
                .build();
        cartStub = CartServiceGrpc.newBlockingStub(channel);
    }

    public CartPaymentResponse getPaymentDetails(Long cartId) {
        CartRequest request = CartRequest.newBuilder()
                .setCartId(cartId)
                .build();

        return cartStub.getPaymentDetails(request);
    }
}
