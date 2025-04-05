package com.tripezzy.eCommerce_service.grpc;

import com.tripezzy.eCommerce_service.dto.CartPaymentDto;
import com.tripezzy.eCommerce_service.exceptions.AccessForbidden;
import com.tripezzy.eCommerce_service.exceptions.ResourceNotFound;
import com.tripezzy.eCommerce_service.exceptions.ServiceUnavailable;
import com.tripezzy.eCommerce_service.services.CartService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class CartGrpcService extends CartServiceGrpc.CartServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(CartGrpcService.class);
    private final CartService cartService;


    public CartGrpcService(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void getPaymentDetails(CartRequest request, StreamObserver<CartPaymentResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for getPaymentDetails, cartId: {}", request.getCartId());

            if (request.getCartId() <= 0) {
                throw new IllegalArgumentException("Invalid cart ID");
            }

            CartPaymentDto cartPayment = cartService.getPaymentDetails(request.getCartId());

            CartPaymentResponse response = CartPaymentResponse.newBuilder()
                    .setAmount(cartPayment.getAmount())
                    .setQuantity(cartPayment.getQuantity())
                    .setName(cartPayment.getName())
                    .setCurrency(cartPayment.getCurrency())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully processed getPaymentDetails request");

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (ResourceNotFound e) {
            log.warn("Cart not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (AccessForbidden e) {
            log.warn("Unauthorized access: {}", e.getMessage());
            responseObserver.onError(Status.PERMISSION_DENIED
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (ServiceUnavailable e) {
            log.error("Service unavailable: {}", e.getMessage());
            responseObserver.onError(Status.UNAVAILABLE
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in getPaymentDetails", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
