package com.tripezzy.booking_service.service;

import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingService {

    BookingDto createBooking(BookingDto bookingDto, Long userId, Long destinationId);

    BookingDto getBookingById(Long bookingId);

    List<BookingDto> getAllBookings();

    Page<BookingDto> getAllBookings(Pageable pageable);

    Page<BookingDto> getBookingsByUserId(Long userId, Pageable pageable);

    Page<BookingDto> getBookingsByDestinationId(Long destinationId, Pageable pageable);

    Page<BookingDto> getBookingsByStatus(Status status, Pageable pageable);

    Page<BookingDto> getBookingsByTravelDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<BookingDto> getBookingsByPaymentStatusAndPriceRange(String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice);

    List<BookingDto> getUpcomingBookingsByStatus(Status status);

    long countBookingsByStatus(Status status);

    void cancelBooking(Long bookingId);

    void softDeleteBooking(Long bookingId);

    Optional<BookingDto> getBookingByReference(String bookingReference);

    List<BookingDto> getBookingsByPaymentStatus(String paymentStatus);

    Page<BookingDto> filterBookings(Long userId, Long destinationId, Status status, String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice, LocalDate startDate, LocalDate endDate, Pageable pageable);

    BookingDto confirmBooking(Long bookingId);

}