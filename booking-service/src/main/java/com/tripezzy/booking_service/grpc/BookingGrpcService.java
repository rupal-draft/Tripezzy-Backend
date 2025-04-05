package com.tripezzy.booking_service.grpc;

import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.dto.BookingPaymentDto;
import com.tripezzy.booking_service.exceptions.IllegalState;
import com.tripezzy.booking_service.exceptions.ResourceNotFound;
import com.tripezzy.booking_service.service.BookingService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class BookingGrpcService extends BookingServiceGrpc.BookingServiceImplBase {


    private static final Logger log = LoggerFactory.getLogger(BookingGrpcService.class);
    private final BookingService bookingService;

    public BookingGrpcService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public void getAllBookings(BookingRequest request, StreamObserver<BookingResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for getAllBookings - page: {}, size: {}",
                    request.getPage(), request.getSize());

            validatePagination(request.getPage(), request.getSize());

            Page<BookingDto> bookingPage = bookingService.getAllBookings(
                    PageRequest.of(request.getPage(), request.getSize()));

            sendBookingResponse(bookingPage, responseObserver);
            log.info("Successfully processed getAllBookings request");

        } catch (StatusRuntimeException e) {
            log.error("gRPC error in getAllBookings: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in getAllBookings", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getBookingsByUserId(UserBookingsRequest request, StreamObserver<BookingResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for getBookingsByUserId - userId: {}, page: {}, size: {}",
                    request.getUserId(), request.getPage(), request.getSize());

            validatePagination(request.getPage(), request.getSize());
            if (request.getUserId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Invalid user ID")
                        .asRuntimeException();
            }

            Page<BookingDto> bookingPage = bookingService.getBookingsByUserId(
                    request.getUserId(), PageRequest.of(request.getPage(), request.getSize()));

            sendBookingResponse(bookingPage, responseObserver);
            log.info("Successfully processed getBookingsByUserId request");

        } catch (StatusRuntimeException e) {
            log.error("gRPC error in getBookingsByUserId: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in getBookingsByUserId", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getBookingsByDestinationId(DestinationBookingsRequest request, StreamObserver<BookingResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for getBookingsByDestinationId - destinationId: {}, page: {}, size: {}",
                    request.getDestinationId(), request.getPage(), request.getSize());

            validatePagination(request.getPage(), request.getSize());
            if (request.getDestinationId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Invalid destination ID")
                        .asRuntimeException();
            }

            Page<BookingDto> bookingPage = bookingService.getBookingsByDestinationId(
                    request.getDestinationId(), PageRequest.of(request.getPage(), request.getSize()));

            sendBookingResponse(bookingPage, responseObserver);
            log.info("Successfully processed getBookingsByDestinationId request");

        } catch (StatusRuntimeException e) {
            log.error("gRPC error in getBookingsByDestinationId: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in getBookingsByDestinationId", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void updateBookingStatus(UpdateStatusRequest request, StreamObserver<BookingResponseSingle> responseObserver) {
        try {
            log.info("Processing gRPC request for updateBookingStatus - bookingId: {}, status: {}",
                    request.getBookingId(), request.getStatus());

            if (request.getBookingId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Invalid booking ID")
                        .asRuntimeException();
            }
            if (request.getStatus().isBlank()) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Status cannot be empty")
                        .asRuntimeException();
            }

            BookingDto updatedBooking = bookingService.updateBookingStatus(request.getBookingId(), request.getStatus());

            BookingResponseSingle response = BookingResponseSingle.newBuilder()
                    .setBooking(convertToGrpcBooking(updatedBooking))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully updated booking status");

        } catch (ResourceNotFound e) {
            log.warn("Booking not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (IllegalState e) {
            log.warn("Invalid state: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (StatusRuntimeException e) {
            log.error("gRPC error in updateBookingStatus: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in updateBookingStatus", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void updateBookingPaymentStatus(UpdateStatusRequest request, StreamObserver<BookingResponseSingle> responseObserver) {
        try {
            log.info("Processing gRPC request for updateBookingPaymentStatus - bookingId: {}, status: {}",
                    request.getBookingId(), request.getStatus());

            if (request.getBookingId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Invalid booking ID")
                        .asRuntimeException();
            }
            if (request.getStatus().isBlank()) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Payment status cannot be empty")
                        .asRuntimeException();
            }

            BookingDto updatedBooking = bookingService.updatePaymentStatus(request.getBookingId(), request.getStatus());

            BookingResponseSingle response = BookingResponseSingle.newBuilder()
                    .setBooking(convertToGrpcBooking(updatedBooking))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully updated booking payment status");

        } catch (ResourceNotFound e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (IllegalState e) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (StatusRuntimeException e) {
            log.error("gRPC error in updateBookingPaymentStatus: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in updateBookingPaymentStatus", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void softDeleteBooking(DeleteBookingRequest request, StreamObserver<DeleteBookingResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for softDeleteBooking - bookingId: {}",
                    request.getBookingId());

            if (request.getBookingId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Invalid booking ID")
                        .asRuntimeException();
            }

            bookingService.softDeleteBooking(request.getBookingId());

            DeleteBookingResponse response = DeleteBookingResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully soft deleted booking");

        } catch (ResourceNotFound e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (StatusRuntimeException e) {
            log.error("gRPC error in softDeleteBooking: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in softDeleteBooking", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getBookingsByPaymentStatus(PaymentStatusRequest request, StreamObserver<BookingResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for getBookingsByPaymentStatus - status: {}",
                    request.getPaymentStatus());

            if (request.getPaymentStatus().isBlank()) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Payment status cannot be empty")
                        .asRuntimeException();
            }

            List<BookingDto> bookingDtos = bookingService.getBookingsByPaymentStatus(request.getPaymentStatus());

            List<Booking> grpcBookings = bookingDtos.stream()
                    .map(this::convertToGrpcBooking)
                    .collect(Collectors.toList());

            BookingResponse response = BookingResponse.newBuilder()
                    .addAllBookings(grpcBookings)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully processed getBookingsByPaymentStatus request");

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid payment status")
                    .asRuntimeException());
        } catch (StatusRuntimeException e) {
            log.error("gRPC error in getBookingsByPaymentStatus: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in getBookingsByPaymentStatus", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getBookingsByStatus(StatusRequest request, StreamObserver<BookingResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for getBookingsByStatus - status: {}, page: {}, size: {}",
                    request.getStatus(), request.getPage(), request.getSize());

            validatePagination(request.getPage(), request.getSize());
            if (request.getStatus().isBlank()) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Status cannot be empty")
                        .asRuntimeException();
            }

            Page<BookingDto> bookingPage = bookingService.getBookingsByStatus(
                    request.getStatus(), PageRequest.of(request.getPage(), request.getSize()));

            sendBookingResponse(bookingPage, responseObserver);
            log.info("Successfully processed getBookingsByStatus request");

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid status")
                    .asRuntimeException());
        } catch (StatusRuntimeException e) {
            log.error("gRPC error in getBookingsByStatus: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in getBookingsByStatus", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void confirmBooking(ConfirmBookingRequest request, StreamObserver<BookingResponseSingle> responseObserver) {
        try {
            log.info("Processing gRPC request for confirmBooking - bookingId: {}",
                    request.getBookingId());

            if (request.getBookingId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Invalid booking ID")
                        .asRuntimeException();
            }

            BookingDto confirmedBooking = bookingService.confirmBooking(request.getBookingId());

            BookingResponseSingle response = BookingResponseSingle.newBuilder()
                    .setBooking(convertToGrpcBooking(confirmedBooking))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully confirmed booking");

        } catch (ResourceNotFound e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (IllegalState e) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (StatusRuntimeException e) {
            log.error("gRPC error in confirmBooking: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in confirmBooking", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getBookingPayment(BookingPaymentRequest request, StreamObserver<BookingPaymentResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for getBookingPayment - bookingId: {}",
                    request.getBookingId());

            if (request.getBookingId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Invalid booking ID")
                        .asRuntimeException();
            }

            BookingPaymentDto booking = bookingService.getBookingPayment(request.getBookingId());

            BookingPaymentResponse response = BookingPaymentResponse.newBuilder()
                    .setAmount(booking.getAmount().doubleValue())
                    .setQuantity(1)
                    .setName(booking.getName())
                    .setCurrency(booking.getCurrency())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully processed getBookingPayment request");

        } catch (ResourceNotFound e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (StatusRuntimeException e) {
            log.error("gRPC error in getBookingPayment: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in getBookingPayment", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    private void sendBookingResponse(Page<BookingDto> bookingPage, StreamObserver<BookingResponse> responseObserver) {
        List<Booking> grpcBookings = bookingPage.getContent().stream().map(bookingDto ->
                Booking.newBuilder()
                        .setBookingId(bookingDto.getId())
                        .setFirstName(bookingDto.getFirstName())
                        .setLastName(bookingDto.getLastName())
                        .setEmail(bookingDto.getEmail())
                        .setPhoneNumber(bookingDto.getPhoneNumber())
                        .setUserId(bookingDto.getUser())
                        .setDestinationId(bookingDto.getDestination())
                        .setTravelDate(bookingDto.getTravelDate().toString())
                        .setTotalPrice(bookingDto.getTotalPrice().doubleValue())
                        .build()
        ).collect(Collectors.toList());

        BookingResponse response = BookingResponse.newBuilder()
                .addAllBookings(grpcBookings)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Booking convertToGrpcBooking(BookingDto dto) {
        return Booking.newBuilder()
                .setBookingId(dto.getId())
                .setFirstName(dto.getFirstName())
                .setLastName(dto.getLastName())
                .setEmail(dto.getEmail())
                .setPhoneNumber(dto.getPhoneNumber())
                .setUserId(dto.getUser())
                .setDestinationId(dto.getDestination())
                .setTravelDate(dto.getTravelDate().toString())
                .setTotalPrice(dto.getTotalPrice().doubleValue())
                .build();
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Page number cannot be negative")
                    .asRuntimeException();
        }
        if (size <= 0 || size > 100) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Page size must be between 1 and 100")
                    .asRuntimeException();
        }
    }
}
