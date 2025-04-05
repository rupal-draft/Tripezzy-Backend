package com.tripezzy.booking_service.service;

import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.dto.BookingPaymentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BookingService {

    BookingDto createBooking(BookingDto bookingDto, Long destinationId);

    BookingDto getBookingById(Long bookingId);

    List<BookingDto> getAllBookings();

    Page<BookingDto> getAllBookings(Pageable pageable);

    Page<BookingDto> getBookingsByUserId(Long userId, Pageable pageable);

    Page<BookingDto> getBookingsByDestinationId(Long destinationId, Pageable pageable);

    Page<BookingDto> getBookingsByStatus(String status, Pageable pageable);

    List<BookingDto> getBookingsByPaymentStatusAndPriceRange(String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice);

    List<BookingDto> getUpcomingBookingsByStatus(String status);

    long countBookingsByStatus(String status);

    void softDeleteBooking(Long bookingId);

    List<BookingDto> getBookingsByPaymentStatus(String paymentStatus);

    Page<BookingDto> filterBookings(Long userId, Long destinationId, String status, String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    BookingDto confirmBooking(Long bookingId);

    BookingPaymentDto getBookingPayment(Long bookingId);

    BookingDto updateBookingStatus(Long bookingId, String status);

    BookingDto updatePaymentStatus(Long bookingId, String paymentStatus);
}