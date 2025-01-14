package com.tripezzy.booking_service.service.implementation;

import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {


    private static final Logger log = Logger.getLogger(BookingServiceImpl.class);

    @Override
    public BookingDto createBooking(BookingDto bookingDto) {
        return null;
    }

    @Override
    public BookingDto getBookingById(Long bookingId) {
        return null;
    }

    @Override
    public List<BookingDto> getAllBookings() {
        return List.of();
    }

    @Override
    public void cancelBooking(Long bookingId) {

    }
}
