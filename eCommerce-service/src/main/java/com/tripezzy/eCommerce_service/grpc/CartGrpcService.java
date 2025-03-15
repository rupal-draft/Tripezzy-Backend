package com.tripezzy.eCommerce_service.grpc;

import com.tripezzy.eCommerce_service.dto.CartPaymentDto;
import com.tripezzy.eCommerce_service.services.CartService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class CartGrpcService extends CartServiceGrpc.CartServiceImplBase {

    private final CartService cartService;

    public CartGrpcService(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void getPaymentDetails(CartRequest request, StreamObserver<CartPaymentResponse> responseObserver) {
        CartPaymentDto cartPayment = cartService.getPaymentDetails(request.getCartId());

        CartPaymentResponse response = CartPaymentResponse.newBuilder()
                .setAmount(cartPayment.getAmount())
                .setQuantity(cartPayment.getQuantity())
                .setName(cartPayment.getName())
                .setCurrency(cartPayment.getCurrency())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
