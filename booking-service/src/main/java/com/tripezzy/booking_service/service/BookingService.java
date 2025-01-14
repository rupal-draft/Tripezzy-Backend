package com.tripezzy.booking_service.service;

import com.tripezzy.booking_service.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(BookingDto bookingDto);

    BookingDto getBookingById(Long bookingId);

    List<BookingDto> getAllBookings();

    void cancelBooking(Long bookingId);

}
