package com.tripezzy.booking_service.service.implementation;

import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.dto.BookingPaymentDto;
import com.tripezzy.booking_service.entity.Booking;
import com.tripezzy.booking_service.entity.enums.Status;
import com.tripezzy.booking_service.exception.ResourceNotFound;
import com.tripezzy.booking_service.repository.BookingRepository;
import com.tripezzy.booking_service.service.BookingService;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = Logger.getLogger(BookingServiceImpl.class);
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;

    public BookingServiceImpl(ModelMapper modelMapper, BookingRepository bookingRepository) {
        this.modelMapper = modelMapper;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public BookingDto createBooking(BookingDto bookingDto, Long userId, Long destinationId) {
        log.info("Creating a new booking for user ID: " + userId);
        Booking booking = modelMapper.map(bookingDto, Booking.class);
        booking.setBookingDate(LocalDateTime.now());
        booking.setUserId(userId);
        booking.setDestinationId(destinationId);
        booking.setStatus(Status.PENDING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: " + savedBooking.getId());
        return modelMapper.map(savedBooking, BookingDto.class);
    }

    @Override
    @Cacheable(value = "booking", key = "#bookingId")
    public BookingDto getBookingById(Long bookingId) {
        log.info("Fetching booking by ID: " + bookingId);
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Cacheable(value = "bookings", key = "bookings")
    public List<BookingDto> getAllBookings() {
        log.info("Fetching all bookings");
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "bookings", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BookingDto> getAllBookings(Pageable pageable) {
        log.info("Fetching all bookings with pagination");
        return bookingRepository.findAll(pageable)
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    @Cacheable(value = "bookingsByUser", key = "#userId")
    public Page<BookingDto> getBookingsByUserId(Long userId, Pageable pageable) {
        log.info("Fetching bookings for user ID: " + userId);
        return bookingRepository.findByUserId(userId, pageable)
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    @Cacheable(value = "bookingsByDestination", key = "#destinationId")
    public Page<BookingDto> getBookingsByDestinationId(Long destinationId, Pageable pageable) {
        log.info("Fetching bookings for destination ID: " + destinationId);
        return bookingRepository.findByDestinationId(destinationId, pageable)
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    @Cacheable(value = "bookingsByStatus", key = "#status")
    public Page<BookingDto> getBookingsByStatus(Status status, Pageable pageable) {
        log.info("Fetching bookings with status: " + status);
        return bookingRepository.findByStatus(status, pageable)
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    @Cacheable(value = "bookingsByTravelDateRange", key = "#startDate.toString() + '-' + #endDate.toString()")
    public Page<BookingDto> getBookingsByTravelDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Fetching bookings between travel dates: " + startDate + " and " + endDate);
        return bookingRepository.findByTravelDateBetween(startDate, endDate, pageable)
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    @Cacheable(value = "bookingsByPaymentStatusAndPriceRange", key = "#paymentStatus + '-' + #minPrice + '-' + #maxPrice")
    public List<BookingDto> getBookingsByPaymentStatusAndPriceRange(String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Fetching bookings with payment status: " + paymentStatus + " and price range: " + minPrice + " to " + maxPrice);
        return bookingRepository.findBookingsByPaymentStatusAndTotalPriceRange(paymentStatus, minPrice, maxPrice).stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "upcomingBookings", key = "#status")
    public List<BookingDto> getUpcomingBookingsByStatus(Status status) {
        log.info("Fetching upcoming bookings with status: " + status);
        return bookingRepository.findUpcomingBookingsByStatus(status).stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public long countBookingsByStatus(Status status) {
        log.info("Counting bookings with status: " + status);
        return bookingRepository.countByStatus(status);
    }

    @Override
    @Transactional
    @CacheEvict(value = "booking", key = "#bookingId")
    public void cancelBooking(Long bookingId) {
        log.info("Cancelling booking with ID: " + bookingId);
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));
        booking.setStatus(Status.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking cancelled successfully with ID: " + bookingId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "booking", key = "#bookingId")
    public void softDeleteBooking(Long bookingId) {
        log.info("Soft deleting booking with ID: " + bookingId);
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));
        bookingRepository.softDeleteById(bookingId);
        log.info("Booking soft deleted successfully with ID: " + bookingId);
    }

    @Override
    @Cacheable(value = "bookingsByPaymentStatus", key = "#paymentStatus")
    public List<BookingDto> getBookingsByPaymentStatus(String paymentStatus) {
        log.info("Fetching bookings by payment status: " + paymentStatus);
        return bookingRepository.findByPaymentStatus(paymentStatus).stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<BookingDto> filterBookings(Long userId, Long destinationId, Status status, String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<Booking> spec = Specification.where(null);

        if (userId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        if (destinationId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("destinationId"), destinationId));
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (paymentStatus != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentStatus"), paymentStatus));
        if (minPrice != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("totalPrice"), minPrice));
        if (maxPrice != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("totalPrice"), maxPrice));
        if (startDate != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("travelDate"), startDate));
        if (endDate != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("travelDate"), endDate));

        return bookingRepository.findAll(spec, pageable).map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    public BookingDto confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFound("Booking not found"));

        if (!booking.getStatus().equals(Status.PENDING)) {
            throw new IllegalStateException("Booking cannot be confirmed");
        }

        booking.setStatus(Status.CONFIRMED);
        bookingRepository.save(booking);

        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    public BookingPaymentDto getBookingPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFound("Booking not found"));

        BookingPaymentDto bookingPaymentDto = new BookingPaymentDto();
        bookingPaymentDto.setAmount(booking.getTotalPrice());
        bookingPaymentDto.setName(booking.getFirstName()+ " " + booking.getLastName());
        bookingPaymentDto.setCurrency("USD");

        return bookingPaymentDto;
    }
}