package com.tripezzy.admin_service.grpc;

import com.tripezzy.admin_service.dto.BookingDto;
import com.tripezzy.booking_service.grpc.*;
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

    @Cacheable(value = "allBookings", key = "'bookings-' + #page + '-' + #size")
    public List<BookingDto> getAllBookings(int page, int size) {
        BookingRequest request = BookingRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();

        BookingResponse response = bookingStub.getAllBookings(request);
        return response.getBookingsList().stream()
                .map(booking -> new BookingDto(
                        booking.getBookingId(),
                        booking.getFirstName(),
                        booking.getLastName(),
                        booking.getEmail(),
                        booking.getPhoneNumber(),
                        booking.getUserId(),
                        booking.getDestinationId(),
                        booking.getTravelDate(),
                        booking.getTotalPrice()
                ))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "bookingsByUserId", key = "bookingsByUserId + #userId + '-' + #page + '-' + #size")
    public List<BookingDto> getBookingsByUserId(Long userId, int page, int size) {
        UserBookingsRequest request = UserBookingsRequest.newBuilder()
                .setUserId(userId)
                .setPage(page)
                .setSize(size)
                .build();

        BookingResponse response = bookingStub.getBookingsByUserId(request);
        return response.getBookingsList().stream()
                .map(booking -> new BookingDto(
                        booking.getBookingId(),
                        booking.getFirstName(),
                        booking.getLastName(),
                        booking.getEmail(),
                        booking.getPhoneNumber(),
                        booking.getUserId(),
                        booking.getDestinationId(),
                        booking.getTravelDate(),
                        booking.getTotalPrice()
                ))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "bookingsByDestinationId", key = "bookingsByDestinationId + #destinationId + '-' + #page + '-' + #size")
    public List<BookingDto> getBookingsByDestinationId(Long destinationId, int page, int size) {
        DestinationBookingsRequest request = DestinationBookingsRequest.newBuilder()
                .setDestinationId(destinationId)
                .setPage(page)
                .setSize(size)
                .build();

        BookingResponse response = bookingStub.getBookingsByDestinationId(request);
        return response.getBookingsList().stream()
                .map(booking -> new BookingDto(
                        booking.getBookingId(),
                        booking.getFirstName(),
                        booking.getLastName(),
                        booking.getEmail(),
                        booking.getPhoneNumber(),
                        booking.getUserId(),
                        booking.getDestinationId(),
                        booking.getTravelDate(),
                        booking.getTotalPrice()
                ))
                .collect(Collectors.toList());
    }

    public boolean softDeleteBooking(Long bookingId) {
        DeleteBookingRequest request = DeleteBookingRequest.newBuilder()
                .setBookingId(bookingId)
                .build();

        DeleteBookingResponse response = bookingStub.softDeleteBooking(request);
        return response.getSuccess();
    }

    @Cacheable(value = "bookingsByPaymentStatus", key = "bookingsByPaymentStatus + #paymentStatus")
    public List<BookingDto> getBookingsByPaymentStatus(String paymentStatus) {
        PaymentStatusRequest request = PaymentStatusRequest.newBuilder()
                .setPaymentStatus(paymentStatus)
                .build();

        BookingResponse response = bookingStub.getBookingsByPaymentStatus(request);
        return response.getBookingsList().stream()
                .map(booking -> new BookingDto(
                        booking.getBookingId(),
                        booking.getFirstName(),
                        booking.getLastName(),
                        booking.getEmail(),
                        booking.getPhoneNumber(),
                        booking.getUserId(),
                        booking.getDestinationId(),
                        booking.getTravelDate(),
                        booking.getTotalPrice()
                ))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "bookingsByPaymentStatus", key = "bookingsByStatus + #status" + '-' + "#page" + '-' + "#size")
    public List<BookingDto> getBookingsByStatus(String status, int page, int size) {
        StatusRequest request = StatusRequest.newBuilder()
                .setStatus(status)
                .setSize(size)
                .setPage(page)
                .build();

        BookingResponse response = bookingStub.getBookingsByStatus(request);
        return response.getBookingsList().stream()
                .map(booking -> new BookingDto(
                        booking.getBookingId(),
                        booking.getFirstName(),
                        booking.getLastName(),
                        booking.getEmail(),
                        booking.getPhoneNumber(),
                        booking.getUserId(),
                        booking.getDestinationId(),
                        booking.getTravelDate(),
                        booking.getTotalPrice()
                ))
                .collect(Collectors.toList());
    }

    public BookingDto confirmBooking(Long bookingId) {
        ConfirmBookingRequest request = ConfirmBookingRequest.newBuilder()
                .setBookingId(bookingId)
                .build();

        BookingResponseSingle response = bookingStub.confirmBooking(request);
        Booking booking = response.getBooking();
        return new BookingDto(
                booking.getBookingId(),
                booking.getFirstName(),
                booking.getLastName(),
                booking.getEmail(),
                booking.getPhoneNumber(),
                booking.getUserId(),
                booking.getDestinationId(),
                booking.getTravelDate(),
                booking.getTotalPrice()
        );
    }
}
