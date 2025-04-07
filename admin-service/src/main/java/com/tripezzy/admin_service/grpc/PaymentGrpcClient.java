package com.tripezzy.admin_service.grpc;

import com.tripezzy.admin_service.dto.PaymentsResponseDto;
import com.tripezzy.admin_service.exceptions.*;
import com.tripezzy.payment_service.grpc.*;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PaymentGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentGrpcClient.class);
    private final PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;
    private final ManagedChannel channel;

    public PaymentGrpcClient() {
        try {
            this.channel = ManagedChannelBuilder
                    .forAddress("payment-service", 4001)
                    .usePlaintext()
                    .build();

            this.paymentStub = PaymentServiceGrpc.newBlockingStub(channel);
        } catch (Exception e) {
            log.error("Failed to initialize gRPC payment client", e);
            throw new ServiceUnavailable("Payment service is currently unavailable");
        }
    }

    private void checkServiceHealth() {
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(
                    HealthCheckRequest.newBuilder().build());

            if (response.getStatus() != HealthCheckResponse.ServingStatus.SERVING) {
                log.error("Payment service is not healthy: {}", response.getStatus());
                throw new ServiceUnavailable("Payment service is not healthy");
            }
            log.info("Payment service health status: {}", response.getStatus());
        } catch (StatusRuntimeException e) {
            log.error("Payment service health check failed", e);
            throw new ServiceUnavailable("Payment service is unreachable");
        }
    }

    @Cacheable(value = "payments", key = "'allPayments'")
    public List<PaymentsResponseDto> getAllPayments() {
        checkServiceHealth();
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            PaymentsResponseList responseList = paymentStub.getAllPayments(request);

            if (responseList == null || responseList.getPaymentsList().isEmpty()) {
                log.warn("No payments found");
                return Collections.emptyList();
            }

            return mapPaymentsResponse(responseList);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get all payments");
            return Collections.emptyList(); // Fallback
        }
    }

    @Cacheable(value = "paymentsByUserId", key = "'paymentsByUserId-' + #userId")
    public List<PaymentsResponseDto> getAllPaymentsByUserId(Long userId) {
        checkServiceHealth();
        if (userId == null || userId <= 0) {
            throw new BadRequestException("Invalid user ID");
        }

        try {
            UserPaymentsRequest request = UserPaymentsRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            PaymentsResponseList responseList = paymentStub.getAllPaymentsByUserId(request);

            if (responseList == null || responseList.getPaymentsList().isEmpty()) {
                log.warn("No payments found for user ID: {}", userId);
                return Collections.emptyList();
            }

            return mapPaymentsResponse(responseList);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get payments by user ID");
            return Collections.emptyList();
        }
    }

    private List<PaymentsResponseDto> mapPaymentsResponse(PaymentsResponseList responseList) {
        return responseList.getPaymentsList().stream()
                .map(this::mapPayment)
                .collect(Collectors.toUnmodifiableList());
    }

    private PaymentsResponseDto mapPayment(PaymentsResponse payment) {
        try {
            return new PaymentsResponseDto(
                    payment.getId(),
                    payment.getUserId(),
                    payment.getReferenceId(),
                    payment.getSessionId(),
                    payment.getStatus(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getName(),
                    payment.getCategory(),
                    payment.getQuantity(),
                    payment.getCreatedAt()
            );
        } catch (Exception e) {
            log.error("Failed to map payment data for ID: {}", payment.getId(), e);
            throw new IllegalState("Failed to process payment data");
        }
    }

    private void handleGrpcException(StatusRuntimeException e, String context) {
        Status.Code code = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        log.error("gRPC error [{}] {}: {}", code, context, description, e);

        switch (code) {
            case NOT_FOUND:
                throw new ResourceNotFound(description != null ? description : "Requested payment not found");
            case INVALID_ARGUMENT:
                throw new BadRequestException(description != null ? description : "Invalid payment request parameters");
            case PERMISSION_DENIED:
                throw new AccessForbidden(description != null ? description : "Payment permission denied");
            case UNAVAILABLE:
                throw new ServiceUnavailable("Payment service is currently unavailable");
            case FAILED_PRECONDITION:
                throw new IllegalState(description != null ? description : "Invalid payment state");
            default:
                throw new ServiceUnavailable("Failed to process payment request");
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
}
