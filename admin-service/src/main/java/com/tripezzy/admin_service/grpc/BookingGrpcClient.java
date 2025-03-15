package com.tripezzy.admin_service.grpc;

import com.tripezzy.booking_service.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Booking> getAllBookings(int page, int size) {
        BookingRequest request = BookingRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();

        BookingResponse response = bookingStub.getAllBookings(request);
        return response.getBookingsList();
    }

    public List<Booking> getBookingsByUserId(Long userId, int page, int size) {
        UserBookingsRequest request = UserBookingsRequest.newBuilder()
                .setUserId(userId)
                .setPage(page)
                .setSize(size)
                .build();

        BookingResponse response = bookingStub.getBookingsByUserId(request);
        return response.getBookingsList();
    }

    public List<Booking> getBookingsByDestinationId(Long destinationId, int page, int size) {
        DestinationBookingsRequest request = DestinationBookingsRequest.newBuilder()
                .setDestinationId(destinationId)
                .setPage(page)
                .setSize(size)
                .build();

        BookingResponse response = bookingStub.getBookingsByDestinationId(request);
        return response.getBookingsList();
    }

    public List<Booking> getBookingsByTravelDateRange(String startDate, String endDate, int page, int size) {
        DateRangeRequest request = DateRangeRequest.newBuilder()
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setPage(page)
                .setSize(size)
                .build();

        BookingResponse response = bookingStub.getBookingsByTravelDateRange(request);
        return response.getBookingsList();
    }

    public boolean softDeleteBooking(Long bookingId) {
        DeleteBookingRequest request = DeleteBookingRequest.newBuilder()
                .setBookingId(bookingId)
                .build();

        DeleteBookingResponse response = bookingStub.softDeleteBooking(request);
        return response.getSuccess();
    }

    public List<Booking> getBookingsByPaymentStatus(String paymentStatus) {
        PaymentStatusRequest request = PaymentStatusRequest.newBuilder()
                .setPaymentStatus(paymentStatus)
                .build();

        BookingResponse response = bookingStub.getBookingsByPaymentStatus(request);
        return response.getBookingsList();
    }

    public Booking confirmBooking(Long bookingId) {
        ConfirmBookingRequest request = ConfirmBookingRequest.newBuilder()
                .setBookingId(bookingId)
                .build();

        BookingResponseSingle response = bookingStub.confirmBooking(request);
        return response.getBooking();
    }
}
