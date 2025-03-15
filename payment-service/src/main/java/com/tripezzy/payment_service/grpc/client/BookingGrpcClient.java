package com.tripezzy.payment_service.grpc.client;

import com.tripezzy.booking_service.grpc.BookingPaymentRequest;
import com.tripezzy.booking_service.grpc.BookingPaymentResponse;
import com.tripezzy.booking_service.grpc.BookingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

@Service
public class BookingGrpcClient {
    private final BookingServiceGrpc.BookingServiceBlockingStub bookingStub;

    public BookingGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("booking-service", 9011)
                .usePlaintext()
                .build();
        bookingStub = BookingServiceGrpc.newBlockingStub(channel);
    }

    public BookingPaymentResponse getBookingPayment(Long bookingId) {
        BookingPaymentRequest request = BookingPaymentRequest.newBuilder()
                .setBookingId(bookingId)
                .build();

        return bookingStub.getBookingPayment(request);
    }
}
