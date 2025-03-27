package com.tripezzy.booking_service.service.implementation;

import com.tripezzy.booking_service.auth.UserContext;
import com.tripezzy.booking_service.auth.UserContextHolder;
import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.dto.BookingPaymentDto;
import com.tripezzy.booking_service.entity.Booking;
import com.tripezzy.booking_service.entity.enums.PaymentStatus;
import com.tripezzy.booking_service.entity.enums.Status;
import com.tripezzy.booking_service.exception.ResourceNotFound;
import com.tripezzy.booking_service.repository.BookingRepository;
import com.tripezzy.booking_service.service.BookingService;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {


    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;

    public BookingServiceImpl(ModelMapper modelMapper, BookingRepository bookingRepository) {
        this.modelMapper = modelMapper;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public BookingDto createBooking(BookingDto bookingDto, Long destinationId) {
        UserContext userContext = UserContextHolder.getUserDetails();
        Long userId = userContext.getUserId();
        log.info("Creating a new booking for user ID: " + userId);

        Booking booking = modelMapper.map(bookingDto, Booking.class);
        booking.setBookingDate(LocalDateTime.now());
        booking.setUser(userId);
        booking.setDestination(destinationId);
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
    @Cacheable(value = "bookings")
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
        return bookingRepository.findByUser(userId, pageable)
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    @Cacheable(value = "bookingsByDestination", key = "#destinationId")
    public Page<BookingDto> getBookingsByDestinationId(Long destinationId, Pageable pageable) {
        log.info("Fetching bookings for destination ID: " + destinationId);
        return bookingRepository.findByDestination(destinationId, pageable)
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    @Cacheable(value = "bookingsByStatus", key = "#status")
    public Page<BookingDto> getBookingsByStatus(String status, Pageable pageable) {
        log.info("Fetching bookings with status: " + status);
        Status bookingStatus = Status.valueOf(status);
        return bookingRepository.findByStatus(bookingStatus, pageable)
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    @Cacheable(value = "bookingsByPaymentStatusAndPriceRange", key = "#paymentStatus + '-' + #minPrice + '-' + #maxPrice")
    public List<BookingDto> getBookingsByPaymentStatusAndPriceRange(String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Fetching bookings with payment status: " + paymentStatus + " and price range: " + minPrice + " to " + maxPrice);
        PaymentStatus paymentStatusEnum = PaymentStatus.valueOf(paymentStatus);
        return bookingRepository.findBookingsByPaymentStatusAndTotalPriceRange(paymentStatusEnum, minPrice, maxPrice).stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "upcomingBookings", key = "#status")
    public List<BookingDto> getUpcomingBookingsByStatus(String status) {
        log.info("Fetching upcoming bookings with status: " + status);
        Status statusEnum = Status.valueOf(status);
        return bookingRepository.findUpcomingBookingsByStatus(statusEnum).stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public long countBookingsByStatus(String status) {
        log.info("Counting bookings with status: " + status);
        return bookingRepository.countByStatus(Status.valueOf(status));
    }


    @Override
    @Transactional
    @CacheEvict(value = "booking", key = "#bookingId")
    public void softDeleteBooking(Long bookingId) {
        log.info("Soft deleting booking with ID: " + bookingId);
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));
        bookingRepository.softDeleteById(bookingId,Status.CANCELLED);
        log.info("Booking soft deleted successfully with ID: " + bookingId);
    }

    @Override
    @Cacheable(value = "bookingsByPaymentStatus", key = "#paymentStatus")
    public List<BookingDto> getBookingsByPaymentStatus(String paymentStatus) {
        log.info("Fetching bookings by payment status: " + paymentStatus);
        PaymentStatus paymentStatusEnum = PaymentStatus.valueOf(paymentStatus);
        return bookingRepository.findByPaymentStatus(paymentStatusEnum).stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<BookingDto> filterBookings(Long userId, Long destinationId, String status, String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice,  Pageable pageable) {
        Specification<Booking> spec = Specification.where((root, query, cb) -> cb.equal(root.get("deleted"), false));
        Status statusEnum = status != null ? Status.valueOf(status) : null;
        PaymentStatus paymentStatusEnum = paymentStatus != null ? PaymentStatus.valueOf(paymentStatus) : null;
        if (userId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("user"), userId));
        if (destinationId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("destination"), destinationId));
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), statusEnum));
        if (paymentStatus != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentStatus"), paymentStatusEnum));
        if (minPrice != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("totalPrice"), minPrice));
        if (maxPrice != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("totalPrice"), maxPrice));


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
        booking.setPaymentStatus(PaymentStatus.PAID);
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