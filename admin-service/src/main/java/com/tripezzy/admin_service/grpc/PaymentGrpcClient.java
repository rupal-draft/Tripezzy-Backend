package com.tripezzy.admin_service.grpc;

import com.tripezzy.admin_service.dto.PaymentsResponseDto;
import com.tripezzy.payment_service.grpc.EmptyRequest;
import com.tripezzy.payment_service.grpc.PaymentServiceGrpc;
import com.tripezzy.payment_service.grpc.PaymentsResponseList;
import com.tripezzy.payment_service.grpc.UserPaymentsRequest;
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
public class PaymentGrpcClient {


    private static final Logger log = LoggerFactory.getLogger(PaymentGrpcClient.class);
    private final PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;

    public PaymentGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 4001)
                .usePlaintext()
                .build();
        paymentStub = PaymentServiceGrpc.newBlockingStub(channel);
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());
            log.info("Health Status: " + response.getStatus());
        } catch (Exception e) {
            log.error("Blog service is unreachable. Proceeding without it.");
        }
    }

    @Cacheable(value = "payments", key = "'allPayments'")
    public List<PaymentsResponseDto> getAllPayments() {
        EmptyRequest request = EmptyRequest.newBuilder().build();
        PaymentsResponseList responseList = paymentStub.getAllPayments(request);
        return responseList.getPaymentsList()
                .stream()
                .map(paymentsResponse ->
                        new PaymentsResponseDto(
                                paymentsResponse.getId(),
                                paymentsResponse.getUserId(),
                                paymentsResponse.getReferenceId(),
                                paymentsResponse.getSessionId(),
                                paymentsResponse.getStatus(),
                                paymentsResponse.getAmount(),
                                paymentsResponse.getCurrency(),
                                paymentsResponse.getName(),
                                paymentsResponse.getCategory(),
                                paymentsResponse.getQuantity(),
                                paymentsResponse.getCreatedAt()
                        )
                )
                .collect(Collectors.toUnmodifiableList());
    }

    @Cacheable(value = "paymentsByUserId", key = "'paymentsByUserId-' + #userId")
    public List<PaymentsResponseDto> getAllPaymentsByUserId(Long userId) {
        UserPaymentsRequest request = UserPaymentsRequest.newBuilder()
                .setUserId(userId)
                .build();

        PaymentsResponseList responseList = paymentStub.getAllPaymentsByUserId(request);
        return responseList.getPaymentsList().stream()
                .map(paymentsResponse ->
                        new PaymentsResponseDto(
                                paymentsResponse.getId(),
                                paymentsResponse.getUserId(),
                                paymentsResponse.getReferenceId(),
                                paymentsResponse.getSessionId(),
                                paymentsResponse.getStatus(),
                                paymentsResponse.getAmount(),
                                paymentsResponse.getCurrency(),
                                paymentsResponse.getName(),
                                paymentsResponse.getCategory(),
                                paymentsResponse.getQuantity(),
                                paymentsResponse.getCreatedAt()
                        )
                )
                .collect(Collectors.toUnmodifiableList());
    }
}
