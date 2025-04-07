package com.tripezzy.admin_service.grpc;

import com.tripezzy.admin_service.dto.BookingDto;
import com.tripezzy.admin_service.exceptions.*;
import com.tripezzy.booking_service.grpc.*;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BookingGrpcClient.class);
    private final BookingServiceGrpc.BookingServiceBlockingStub bookingStub;
    private final ManagedChannel channel;

    public BookingGrpcClient() {
        try {
            this.channel = ManagedChannelBuilder
                    .forAddress("booking-service", 9011)
                    .usePlaintext()
                    .build();

            this.bookingStub = BookingServiceGrpc.newBlockingStub(channel);
        } catch (Exception e) {
            log.error("Failed to initialize gRPC client", e);
            throw new ServiceUnavailable("Booking service is currently unavailable");
        }
    }

    private void checkServiceHealth() {
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(
                    HealthCheckRequest.newBuilder().build());

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

    @Cacheable(value = "allBookings", key = "'bookings-' + #page + '-' + #size")
    public List<BookingDto> getAllBookings(int page, int size) {
        checkServiceHealth();
        validatePaginationParams(page, size);

        try {
            BookingRequest request = BookingRequest.newBuilder()
                    .setPage(page)
                    .setSize(size)
                    .build();

            BookingResponse response = bookingStub.getAllBookings(request);
            return mapBookingResponse(response);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get all bookings");
            return Collections.emptyList(); // Fallback
        }
    }

    @Cacheable(value = "bookingsByUserId", key = "'bookingsByUserId-' + #userId + '-' + #page + '-' + #size")
    public List<BookingDto> getBookingsByUserId(Long userId, int page, int size) {
        checkServiceHealth();
        validatePaginationParams(page, size);
        if (userId == null || userId <= 0) {
            throw new BadRequestException("Invalid user ID");
        }

        try {
            UserBookingsRequest request = UserBookingsRequest.newBuilder()
                    .setUserId(userId)
                    .setPage(page)
                    .setSize(size)
                    .build();

            BookingResponse response = bookingStub.getBookingsByUserId(request);
            return mapBookingResponse(response);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get bookings by user ID");
            return Collections.emptyList(); // Fallback
        }
    }

    @Cacheable(value = "bookingsByDestinationId", key = "'bookingsByDestinationId-' + #destinationId + '-' + #page + '-' + #size")
    public List<BookingDto> getBookingsByDestinationId(Long destinationId, int page, int size) {
        checkServiceHealth();
        validatePaginationParams(page, size);
        if (destinationId == null || destinationId <= 0) {
            throw new BadRequestException("Invalid destination ID");
        }

        try {
            DestinationBookingsRequest request = DestinationBookingsRequest.newBuilder()
                    .setDestinationId(destinationId)
                    .setPage(page)
                    .setSize(size)
                    .build();

            BookingResponse response = bookingStub.getBookingsByDestinationId(request);
            return mapBookingResponse(response);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get bookings by destination ID");
            return Collections.emptyList(); // Fallback
        }
    }

    @CacheEvict(value = {"allBookings", "bookingsByUserId", "bookingsByDestinationId",
            "bookingsByPaymentStatus", "bookingsByStatus"}, allEntries = true)
    public boolean softDeleteBooking(Long bookingId) {
        checkServiceHealth();
        if (bookingId == null || bookingId <= 0) {
            throw new BadRequestException("Invalid booking ID");
        }

        try {
            DeleteBookingRequest request = DeleteBookingRequest.newBuilder()
                    .setBookingId(bookingId)
                    .build();

            DeleteBookingResponse response = bookingStub.softDeleteBooking(request);
            return response.getSuccess();
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to soft delete booking");
            throw new ServiceUnavailable("Failed to delete booking. Please try again later");
        }
    }

    @Cacheable(value = "bookingsByPaymentStatus", key = "'bookingsByPaymentStatus-' + #paymentStatus")
    public List<BookingDto> getBookingsByPaymentStatus(String paymentStatus) {
        checkServiceHealth();
        if (paymentStatus == null || paymentStatus.isBlank()) {
            throw new BadRequestException("Payment status cannot be empty");
        }

        try {
            PaymentStatusRequest request = PaymentStatusRequest.newBuilder()
                    .setPaymentStatus(paymentStatus)
                    .build();

            BookingResponse response = bookingStub.getBookingsByPaymentStatus(request);
            return mapBookingResponse(response);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get bookings by payment status");
            return Collections.emptyList(); // Fallback
        }
    }

    @Cacheable(value = "bookingsByStatus", key = "'bookingsByStatus-' + #status + '-' + #page + '-' + #size")
    public List<BookingDto> getBookingsByStatus(String status, int page, int size) {
        checkServiceHealth();
        validatePaginationParams(page, size);
        if (status == null || status.isBlank()) {
            throw new BadRequestException("Status cannot be empty");
        }

        try {
            StatusRequest request = StatusRequest.newBuilder()
                    .setStatus(status)
                    .setSize(size)
                    .setPage(page)
                    .build();

            BookingResponse response = bookingStub.getBookingsByStatus(request);
            return mapBookingResponse(response);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get bookings by status");
            return Collections.emptyList();
        }
    }

    @CacheEvict(value = {"allBookings", "bookingsByUserId", "bookingsByStatus"}, allEntries = true)
    public BookingDto confirmBooking(Long bookingId) {
        checkServiceHealth();
        if (bookingId == null || bookingId <= 0) {
            throw new BadRequestException("Invalid booking ID");
        }

        try {
            ConfirmBookingRequest request = ConfirmBookingRequest.newBuilder()
                    .setBookingId(bookingId)
                    .build();

            BookingResponseSingle response = bookingStub.confirmBooking(request);
            return mapBooking(response.getBooking());
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to confirm booking");
            throw new ServiceUnavailable("Failed to confirm booking. Please try again later");
        }
    }

    @CacheEvict(value = {"allBookings", "bookingsByUserId", "bookingsByStatus"}, allEntries = true)
    public BookingDto updateBookingStatus(Long bookingId, String status) {
        checkServiceHealth();
        if (bookingId == null || bookingId <= 0) {
            throw new BadRequestException("Invalid booking ID");
        }
        if (status == null || status.isBlank()) {
            throw new BadRequestException("Status cannot be empty");
        }

        try {
            UpdateStatusRequest request = UpdateStatusRequest.newBuilder()
                    .setBookingId(bookingId)
                    .setStatus(status)
                    .build();

            BookingResponseSingle response = bookingStub.updateBookingStatus(request);
            return mapBooking(response.getBooking());
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to update booking status");
            throw new ServiceUnavailable("Failed to update booking status. Please try again later");
        }
    }

    @CacheEvict(value = {"allBookings", "bookingsByUserId", "bookingsByStatus"}, allEntries = true)
    public BookingDto updateBookingPaymentStatus(Long bookingId, String paymentStatus) {
        checkServiceHealth();
        if (bookingId == null || bookingId <= 0) {
            throw new BadRequestException("Invalid booking ID");
        }
        if (paymentStatus == null || paymentStatus.isBlank()) {
            throw new BadRequestException("Payment status cannot be empty");
        }

        try {
            UpdateStatusRequest request = UpdateStatusRequest.newBuilder()
                    .setBookingId(bookingId)
                    .setStatus(paymentStatus)
                    .build();

            BookingResponseSingle response = bookingStub.updateBookingPaymentStatus(request);
            return mapBooking(response.getBooking());
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to update booking payment status");
            throw new ServiceUnavailable("Failed to update booking payment status. Please try again later");
        }
    }

    private List<BookingDto> mapBookingResponse(BookingResponse response) {
        return response.getBookingsList().stream()
                .map(this::mapBooking)
                .collect(Collectors.toList());
    }

    private BookingDto mapBooking(Booking booking) {
        try {
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
        } catch (Exception e) {
            log.error("Failed to map booking data", e);
            throw new IllegalState("Failed to process booking data");
        }
    }

    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }

    private void handleGrpcException(StatusRuntimeException e, String context) {
        Status.Code code = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        log.error("gRPC error [{}] {}: {}", code, context, description, e);

        switch (code) {
            case NOT_FOUND:
                throw new ResourceNotFound(description != null ? description : "Requested resource not found");
            case INVALID_ARGUMENT:
                throw new BadRequestException(description != null ? description : "Invalid request parameters");
            case PERMISSION_DENIED:
                throw new AccessForbidden(description != null ? description : "Permission denied");
            case UNAVAILABLE:
                throw new ServiceUnavailable("Booking service is currently unavailable");
            default:
                throw new ServiceUnavailable("Failed to process booking request");
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
