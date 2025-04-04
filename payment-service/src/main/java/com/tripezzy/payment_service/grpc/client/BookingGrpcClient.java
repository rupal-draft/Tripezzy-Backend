package com.tripezzy.payment_service.grpc.client;

import com.tripezzy.booking_service.grpc.BookingPaymentRequest;
import com.tripezzy.booking_service.grpc.BookingPaymentResponse;
import com.tripezzy.booking_service.grpc.BookingServiceGrpc;
import com.tripezzy.payment_service.dto.BookingPaymentRequestDto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookingGrpcClient {


    private static final Logger log = LoggerFactory.getLogger(BookingGrpcClient.class);
    private final BookingServiceGrpc.BookingServiceBlockingStub bookingStub;

    public BookingGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9011)
                .usePlaintext()
                .build();
        bookingStub = BookingServiceGrpc.newBlockingStub(channel);
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());
            log.info("Health Status: " + response.getStatus());
        } catch (Exception e) {
            log.error("Blog service is unreachable. Proceeding without it.");
        }
    }

    public BookingPaymentRequestDto getBookingPayment(Long bookingId) {
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
    }
}
