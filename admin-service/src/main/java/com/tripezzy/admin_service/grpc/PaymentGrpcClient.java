package com.tripezzy.admin_service.grpc;

import com.tripezzy.payment_service.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentGrpcClient {
    private final PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;

    public PaymentGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("payment-service", 4001)
                .usePlaintext()
                .build();
        paymentStub = PaymentServiceGrpc.newBlockingStub(channel);
    }

    @Cacheable(value = "payments" , key = "payments")
    public List<PaymentsResponse> getAllPayments() {
        EmptyRequest request = EmptyRequest.newBuilder().build();
        PaymentsResponseList responseList = paymentStub.getAllPayments(request);
        return responseList.getPaymentsList();
    }

    @Cacheable(value = "paymentsByUserId", key = "paymentsByUserId + #userId")
    public List<PaymentsResponse> getAllPaymentsByUserId(Long userId) {
        UserPaymentsRequest request = UserPaymentsRequest.newBuilder()
                .setUserId(userId)
                .build();

        PaymentsResponseList responseList = paymentStub.getAllPaymentsByUserId(request);
        return responseList.getPaymentsList();
    }
}
