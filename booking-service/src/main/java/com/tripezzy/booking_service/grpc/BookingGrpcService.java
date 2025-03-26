package com.tripezzy.booking_service.grpc;

import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.dto.BookingPaymentDto;
import com.tripezzy.booking_service.service.BookingService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class BookingGrpcService extends BookingServiceGrpc.BookingServiceImplBase {

    private final BookingService bookingService;

    public BookingGrpcService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public void getAllBookings(BookingRequest request, StreamObserver<BookingResponse> responseObserver) {
        Page<BookingDto> bookingPage = bookingService.getAllBookings(PageRequest.of(request.getPage(), request.getSize()));

        sendBookingResponse(bookingPage, responseObserver);
    }

    @Override
    public void getBookingsByUserId(UserBookingsRequest request, StreamObserver<BookingResponse> responseObserver) {
        Page<BookingDto> bookingPage = bookingService.getBookingsByUserId(
                request.getUserId(), PageRequest.of(request.getPage(), request.getSize()));

        sendBookingResponse(bookingPage, responseObserver);
    }

    @Override
    public void getBookingsByDestinationId(DestinationBookingsRequest request, StreamObserver<BookingResponse> responseObserver) {
        Page<BookingDto> bookingPage = bookingService.getBookingsByDestinationId(
                request.getDestinationId(), PageRequest.of(request.getPage(), request.getSize()));

        sendBookingResponse(bookingPage, responseObserver);
    }


    @Override
    public void softDeleteBooking(DeleteBookingRequest request, StreamObserver<DeleteBookingResponse> responseObserver) {
        bookingService.softDeleteBooking(request.getBookingId());

        DeleteBookingResponse response = DeleteBookingResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookingsByPaymentStatus(PaymentStatusRequest request, StreamObserver<BookingResponse> responseObserver) {
        List<BookingDto> bookingDtos = bookingService.getBookingsByPaymentStatus(request.getPaymentStatus());

        List<Booking> grpcBookings = bookingDtos.stream()
                .map(this::convertToGrpcBooking)
                .collect(Collectors.toList());

        BookingResponse response = BookingResponse.newBuilder()
                .addAllBookings(grpcBookings)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void confirmBooking(ConfirmBookingRequest request, StreamObserver<BookingResponseSingle> responseObserver) {
        BookingDto confirmedBooking = bookingService.confirmBooking(request.getBookingId());

        BookingResponseSingle response = BookingResponseSingle.newBuilder()
                .setBooking(convertToGrpcBooking(confirmedBooking))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookingPayment(BookingPaymentRequest request, StreamObserver<BookingPaymentResponse> responseObserver) {
        BookingPaymentDto booking = bookingService.getBookingPayment(request.getBookingId());

        BookingPaymentResponse response = BookingPaymentResponse.newBuilder()
                .setAmount(booking.getAmount().doubleValue())
                .setQuantity(1)
                .setName(booking.getName())
                .setCurrency(booking.getCurrency())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
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
}
