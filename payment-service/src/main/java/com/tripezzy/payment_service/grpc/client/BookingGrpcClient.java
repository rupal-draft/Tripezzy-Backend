package com.tripezzy.payment_service.grpc.client;

import com.tripezzy.booking_service.grpc.BookingPaymentRequest;
import com.tripezzy.booking_service.grpc.BookingPaymentResponse;
import com.tripezzy.booking_service.grpc.BookingServiceGrpc;
import com.tripezzy.payment_service.dto.BookingPaymentRequestDto;
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
public class BookingGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BookingGrpcClient.class);
    private final BookingServiceGrpc.BookingServiceBlockingStub bookingStub;
    private final ManagedChannel channel;

    public BookingGrpcClient() {
        this.channel = ManagedChannelBuilder
                .forAddress("localhost", 9011)
                .usePlaintext()
                .build();
        this.bookingStub = BookingServiceGrpc.newBlockingStub(channel);
        checkServiceHealth();
    }

    private void checkServiceHealth() {
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());
            if (response.getStatus() != HealthCheckResponse.ServingStatus.SERVING) {
                log.error("Booking service is not healthy: {}", response.getStatus());
                throw new ServiceUnavailable("Booking service is not healthy");
            }
            log.info("Booking service health status: {}", response.getStatus());
        } catch (StatusRuntimeException e) {
            log.error("Booking service health check failed", e);
            throw new ServiceUnavailable("Booking service is unreachable");
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

    public BookingPaymentRequestDto getBookingPayment(Long bookingId) {
        try {
            log.info("Fetching booking payment details for booking ID: {}", bookingId);

            if (bookingId == null || bookingId <= 0) {
                throw new IllegalArgumentException("Invalid booking ID");
            }

            BookingPaymentRequest request = BookingPaymentRequest.newBuilder()
                    .setBookingId(bookingId)
                    .build();

            BookingPaymentResponse response = bookingStub.getBookingPayment(request);

            return new BookingPaymentRequestDto(
                    response.getAmount(),
                    response.getQuantity(),
                    response.getName(),
                    response.getCurrency()
            );
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get booking payment");
            throw new ServiceUnavailable("Unable to retrieve booking payment at this time");
        }
    }

    private void handleGrpcException(StatusRuntimeException e, String context) {
        Status.Code code = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        log.error("gRPC error [{}] {}: {}", code, context, description, e);

        switch (code) {
            case NOT_FOUND:
                throw new ResourceNotFound(description != null ? description : "Booking not found");
            case INVALID_ARGUMENT:
                throw new IllegalArgumentException(description != null ? description : "Invalid request parameters");
            case UNAVAILABLE:
                throw new ServiceUnavailable("Booking service is currently unavailable");
            default:
                throw new ServiceUnavailable("Failed to process booking request");
        }
    }
}
