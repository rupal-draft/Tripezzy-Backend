package com.tripezzy.booking_service.service.implementation;

import com.tripezzy.booking_service.auth.UserContext;
import com.tripezzy.booking_service.auth.UserContextHolder;
import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.dto.BookingPaymentDto;
import com.tripezzy.booking_service.entity.Booking;
import com.tripezzy.booking_service.entity.enums.PaymentStatus;
import com.tripezzy.booking_service.entity.enums.Status;
import com.tripezzy.booking_service.events.BookingConfirmedEvent;
import com.tripezzy.booking_service.events.BookingCreatedEvent;
import com.tripezzy.booking_service.events.BookingStatusUpdatedEvent;
import com.tripezzy.booking_service.exceptions.*;
import com.tripezzy.booking_service.repository.BookingRepository;
import com.tripezzy.booking_service.service.BookingService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {


    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;
    private final KafkaTemplate<Long, BookingCreatedEvent> kafkaBookingCreatedTemplate;
    private final KafkaTemplate<Long, BookingStatusUpdatedEvent> bookingStatusUpdatedKafkaTemplate;
    private final KafkaTemplate<Long, BookingConfirmedEvent> bookingConfirmedKafkaTemplate;

    public BookingServiceImpl(ModelMapper modelMapper,
                              BookingRepository bookingRepository, KafkaTemplate<Long, BookingCreatedEvent> kafkaTemplate,
                              KafkaTemplate<Long, BookingStatusUpdatedEvent> bookingStatusUpdatedEventKafkaTemplate,
                              KafkaTemplate<Long, BookingConfirmedEvent> bookingConfirmedKafkaTemplate) {
        this.modelMapper = modelMapper;
        this.bookingRepository = bookingRepository;
        this.kafkaBookingCreatedTemplate = kafkaTemplate;
        this.bookingStatusUpdatedKafkaTemplate = bookingStatusUpdatedEventKafkaTemplate;
        this.bookingConfirmedKafkaTemplate = bookingConfirmedKafkaTemplate;
    }

    @Override
    @Transactional
    public BookingDto createBooking(BookingDto bookingDto, Long destinationId) {
        try {
            log.info("Creating new booking for destination ID: {}", destinationId);

            if (bookingDto == null) {
                throw new BadRequestException("Booking data cannot be null");
            }
            if (destinationId == null || destinationId <= 0) {
                throw new BadRequestException("Invalid destination ID");
            }
            if (bookingDto.getTotalPrice() == null || bookingDto.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Price must be greater than zero");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Booking booking = modelMapper.map(bookingDto, Booking.class);
            booking.setBookingDate(LocalDateTime.now());
            booking.setUser(userContext.getUserId());
            booking.setDestination(destinationId);
            booking.setStatus(Status.PENDING);

            Booking savedBooking = bookingRepository.save(booking);

            try {
                log.info("Sending booking to Kafka with ID: {}", savedBooking.getId());

                BookingCreatedEvent event = new BookingCreatedEvent();
                event.setUser(userContext.getUserId());
                event.setDestination(destinationId);
                event.setBookingDate(savedBooking.getBookingDate().toString());
                event.setTravelDate(savedBooking.getTravelDate().toString());
                event.setTotalPrice(savedBooking.getTotalPrice());
                event.setBooking(savedBooking.getId());

                kafkaBookingCreatedTemplate.send("new-booking", savedBooking.getId(), event);

                log.info("Booking sent to Kafka with ID: {}", savedBooking.getId());
            } catch (KafkaException ex) {
                log.error("Kafka error while creating booking", ex);
                throw new KafkaException("Failed to send booking to Kafka");
            }


            log.info("Booking created successfully with ID: {}", savedBooking.getId());

            return modelMapper.map(savedBooking, BookingDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while creating booking", ex);
            throw new DataIntegrityViolation("Failed to create booking due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error while creating booking", ex);
            throw new IllegalState("Failed to map booking data");
        }
    }

    @Override
    @Cacheable(value = "booking", key = "#bookingId")
    public BookingDto getBookingById(Long bookingId) {
        try {
            log.info("Fetching booking by ID: {}", bookingId);

            if (bookingId == null || bookingId <= 0) {
                throw new BadRequestException("Invalid booking ID");
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));

            return modelMapper.map(booking, BookingDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching booking ID {}", bookingId, ex);
            throw new ServiceUnavailable("Unable to retrieve booking at this time");
        }
    }

    @Override
    @Cacheable(value = "bookings")
    public List<BookingDto> getAllBookings() {
        try {
            log.info("Fetching all bookings");

            List<Booking> bookings = bookingRepository.findAll();
            return bookings.stream()
                    .map(booking -> {
                        try {
                            return modelMapper.map(booking, BookingDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for booking ID {}", booking.getId(), ex);
                            throw new IllegalState("Failed to map booking data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while fetching all bookings", ex);
            throw new ServiceUnavailable("Unable to retrieve bookings at this time");
        }
    }

    @Override
    @Cacheable(value = "bookings", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BookingDto> getAllBookings(Pageable pageable) {
        try {
            log.info("Fetching all bookings with pagination");

            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return bookingRepository.findAll(pageable)
                    .map(booking -> {
                        try {
                            return modelMapper.map(booking, BookingDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for booking ID {}", booking.getId(), ex);
                            throw new IllegalState("Failed to map booking data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching paginated bookings", ex);
            throw new ServiceUnavailable("Unable to retrieve bookings at this time");
        }
    }

    @Override
    @Cacheable(value = "bookingsByUser", key = "#userId")
    public Page<BookingDto> getBookingsByUserId(Long userId, Pageable pageable) {
        try {
            log.info("Fetching bookings for user ID: {}", userId);

            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID");
            }
            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return bookingRepository.findByUser(userId, pageable)
                    .map(booking -> {
                        try {
                            return modelMapper.map(booking, BookingDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for booking ID {}", booking.getId(), ex);
                            throw new IllegalState("Failed to map booking data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching bookings for user ID {}", userId, ex);
            throw new ServiceUnavailable("Unable to retrieve bookings at this time");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "booking", key = "#bookingId")
    public void softDeleteBooking(Long bookingId) {
        try {
            log.info("Soft deleting booking with ID: {}", bookingId);

            if (bookingId == null || bookingId <= 0) {
                throw new BadRequestException("Invalid booking ID");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));

            // Authorization check
            if (!booking.getUser().equals(userContext.getUserId())) {
                throw new AccessForbidden("You are not authorized to delete this booking");
            }

            bookingRepository.softDeleteById(bookingId, Status.CANCELLED);
            log.info("Booking soft deleted successfully with ID: {}", bookingId);

        } catch (DataAccessException ex) {
            log.error("Database error while soft deleting booking ID {}", bookingId, ex);
            throw new DataIntegrityViolation("Failed to delete booking due to database error");
        }
    }

    @Override
    @Transactional
    public BookingDto confirmBooking(Long bookingId) {
        try {
            log.info("Confirming booking with ID: {}", bookingId);

            if (bookingId == null || bookingId <= 0) {
                throw new BadRequestException("Invalid booking ID");
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));

            booking.setStatus(Status.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.PAID);
            Booking updatedBooking = bookingRepository.save(booking);

            log.info("Sending booking confirmed event for booking ID: {}", bookingId);
            BookingConfirmedEvent bookingConfirmedEvent = new BookingConfirmedEvent();
            bookingConfirmedEvent.setBooking(updatedBooking.getId());
            bookingConfirmedEvent.setUser(updatedBooking.getUser());

            bookingConfirmedKafkaTemplate.send("booking-confirmed", booking.getId(), bookingConfirmedEvent);
            log.trace("Booking confirmed event sent for booking ID: {}", bookingId);

            log.info("Booking confirmed successfully with ID: {}", bookingId);
            return modelMapper.map(updatedBooking, BookingDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while confirming booking ID {}", bookingId, ex);
            throw new DataIntegrityViolation("Failed to confirm booking due to database error");
        } catch (KafkaException ex) {
            log.error("Kafka error while confirming booking ID {}", bookingId, ex);
            throw new KafkaException("Failed to confirm booking due to Kafka error");
        }
    }

    @Override
    @Transactional
    public BookingPaymentDto getBookingPayment(Long bookingId) {
        try {
            log.info("Fetching payment details for booking ID: {}", bookingId);

            if (bookingId == null || bookingId <= 0) {
                throw new BadRequestException("Invalid booking ID");
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));

            BookingPaymentDto bookingPaymentDto = new BookingPaymentDto();
            bookingPaymentDto.setAmount(booking.getTotalPrice());
            bookingPaymentDto.setName(booking.getFirstName() + " " + booking.getLastName());
            bookingPaymentDto.setCurrency("USD");

            return bookingPaymentDto;

        } catch (DataAccessException ex) {
            log.error("Database error while fetching payment for booking ID {}", bookingId, ex);
            throw new ServiceUnavailable("Unable to retrieve payment details at this time");
        }
    }

    @Override
    @Cacheable(value = "bookingsByDestination", key = "#destinationId")
    public Page<BookingDto> getBookingsByDestinationId(Long destinationId, Pageable pageable) {
        try {
            log.info("Fetching bookings for destination ID: {}", destinationId);

            if (destinationId == null || destinationId <= 0) {
                throw new BadRequestException("Invalid destination ID");
            }
            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return bookingRepository.findByDestination(destinationId, pageable)
                    .map(booking -> {
                        try {
                            return modelMapper.map(booking, BookingDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for booking ID {}", booking.getId(), ex);
                            throw new IllegalState("Failed to map booking data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching bookings for destination ID {}", destinationId, ex);
            throw new ServiceUnavailable("Unable to retrieve bookings at this time");
        }
    }

    @Override
    @Cacheable(value = "bookingsByStatus", key = "#status")
    public Page<BookingDto> getBookingsByStatus(String status, Pageable pageable) {
        try {
            log.info("Fetching bookings with status: {}", status);

            if (status == null || status.isBlank()) {
                throw new BadRequestException("Status cannot be empty");
            }
            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            Status bookingStatus;
            try {
                bookingStatus = Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status value: " + status);
            }

            return bookingRepository.findByStatus(bookingStatus, pageable)
                    .map(booking -> {
                        try {
                            return modelMapper.map(booking, BookingDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for booking ID {}", booking.getId(), ex);
                            throw new IllegalState("Failed to map booking data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching bookings by status {}", status, ex);
            throw new ServiceUnavailable("Unable to retrieve bookings at this time");
        }
    }

    @Override
    public Page<BookingDto> filterBookings(Long userId, Long destinationId, String status,
                                           String paymentStatus, BigDecimal minPrice,
                                           BigDecimal maxPrice, Pageable pageable) {
        try {
            log.info("Filtering bookings with parameters - userId: {}, destinationId: {}, status: {}, paymentStatus: {}, minPrice: {}, maxPrice: {}",
                    userId, destinationId, status, paymentStatus, minPrice, maxPrice);

            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }
            if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
                throw new BadRequestException("Minimum price cannot be greater than maximum price");
            }

            Specification<Booking> spec = Specification.where((root, query, cb) -> cb.equal(root.get("deleted"), false));

            if (userId != null) {
                if (userId <= 0) throw new BadRequestException("Invalid user ID");
                spec = spec.and((root, query, cb) -> cb.equal(root.get("user"), userId));
            }

            if (destinationId != null) {
                if (destinationId <= 0) throw new BadRequestException("Invalid destination ID");
                spec = spec.and((root, query, cb) -> cb.equal(root.get("destination"), destinationId));
            }

            if (status != null) {
                try {
                    Status statusEnum = Status.valueOf(status);
                    spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid status value: " + status);
                }
            }

            if (paymentStatus != null) {
                try {
                    PaymentStatus paymentStatusEnum = PaymentStatus.valueOf(paymentStatus);
                    spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentStatus"), paymentStatusEnum));
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid payment status value: " + paymentStatus);
                }
            }

            if (minPrice != null) {
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("totalPrice"), minPrice));
            }

            if (maxPrice != null) {
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("totalPrice"), maxPrice));
            }

            return bookingRepository.findAll(spec, pageable)
                    .map(booking -> {
                        try {
                            return modelMapper.map(booking, BookingDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for booking ID {}", booking.getId(), ex);
                            throw new IllegalState("Failed to map booking data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while filtering bookings", ex);
            throw new ServiceUnavailable("Unable to filter bookings at this time");
        }
    }

    @Override
    @Cacheable(value = "bookingsByPaymentStatusAndPriceRange",
            key = "#paymentStatus + '-' + #minPrice + '-' + #maxPrice")
    public List<BookingDto> getBookingsByPaymentStatusAndPriceRange(String paymentStatus,
                                                                    BigDecimal minPrice,
                                                                    BigDecimal maxPrice) {
        try {
            log.info("Fetching bookings with payment status: {} and price range: {} to {}",
                    paymentStatus, minPrice, maxPrice);

            // Validate inputs
            if (paymentStatus == null || paymentStatus.isBlank()) {
                throw new BadRequestException("Payment status cannot be empty");
            }
            if (minPrice == null || minPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Minimum price must be positive");
            }
            if (maxPrice == null || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Maximum price must be positive");
            }
            if (minPrice.compareTo(maxPrice) > 0) {
                throw new BadRequestException("Minimum price cannot be greater than maximum price");
            }

            PaymentStatus paymentStatusEnum;
            try {
                paymentStatusEnum = PaymentStatus.valueOf(paymentStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid payment status: " + paymentStatus);
            }

            List<Booking> bookings = bookingRepository
                    .findBookingsByPaymentStatusAndTotalPriceRange(paymentStatusEnum, minPrice, maxPrice);

            return bookings.stream()
                    .map(booking -> {
                        try {
                            return modelMapper.map(booking, BookingDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for booking ID {}", booking.getId(), ex);
                            throw new IllegalState("Failed to map booking data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while fetching bookings by payment status and price range", ex);
            throw new ServiceUnavailable("Unable to retrieve bookings at this time");
        }
    }

    @Override
    @Cacheable(value = "upcomingBookings", key = "#status")
    public List<BookingDto> getUpcomingBookingsByStatus(String status) {
        try {
            log.info("Fetching upcoming bookings with status: {}", status);

            if (status == null || status.isBlank()) {
                throw new BadRequestException("Status cannot be empty");
            }

            Status statusEnum;
            try {
                statusEnum = Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status);
            }

            List<Booking> bookings = bookingRepository.findUpcomingBookingsByStatus(statusEnum);
            return bookings.stream()
                    .map(booking -> {
                        try {
                            return modelMapper.map(booking, BookingDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for booking ID {}", booking.getId(), ex);
                            throw new IllegalState("Failed to map booking data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while fetching upcoming bookings", ex);
            throw new ServiceUnavailable("Unable to retrieve upcoming bookings at this time");
        }
    }

    @Override
    public long countBookingsByStatus(String status) {
        try {
            log.info("Counting bookings with status: {}", status);

            if (status == null || status.isBlank()) {
                throw new BadRequestException("Status cannot be empty");
            }

            Status statusEnum;
            try {
                statusEnum = Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status);
            }

            return bookingRepository.countByStatus(statusEnum);

        } catch (DataAccessException ex) {
            log.error("Database error while counting bookings by status", ex);
            throw new ServiceUnavailable("Unable to count bookings at this time");
        }
    }

    @Override
    @Cacheable(value = "bookingsByPaymentStatus", key = "#paymentStatus")
    public List<BookingDto> getBookingsByPaymentStatus(String paymentStatus) {
        try {
            log.info("Fetching bookings by payment status: {}", paymentStatus);

            if (paymentStatus == null || paymentStatus.isBlank()) {
                throw new BadRequestException("Payment status cannot be empty");
            }

            PaymentStatus paymentStatusEnum;
            try {
                paymentStatusEnum = PaymentStatus.valueOf(paymentStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid payment status: " + paymentStatus);
            }

            List<Booking> bookings = bookingRepository.findByPaymentStatus(paymentStatusEnum);
            return bookings.stream()
                    .map(booking -> {
                        try {
                            return modelMapper.map(booking, BookingDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for booking ID {}", booking.getId(), ex);
                            throw new IllegalState("Failed to map booking data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while fetching bookings by payment status", ex);
            throw new ServiceUnavailable("Unable to retrieve bookings at this time");
        }
    }

    @Override
    @Transactional
    public BookingDto updateBookingStatus(Long bookingId, String status) {
        try {
            log.info("Updating status for booking ID: {} to {}", bookingId, status);

            if (bookingId == null || bookingId <= 0) {
                throw new BadRequestException("Invalid booking ID");
            }
            if (status == null || status.isBlank()) {
                throw new BadRequestException("Status cannot be empty");
            }

            Status statusEnum;
            try {
                statusEnum = Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status);
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));

            if (booking.getStatus() == Status.CANCELLED) {
                throw new IllegalState("Cannot update status of a cancelled booking");
            }

            booking.setStatus(statusEnum);
            Booking updatedBooking = bookingRepository.save(booking);

            try {
                log.info("Sending Kafka event for booking status update");
                BookingStatusUpdatedEvent event = new BookingStatusUpdatedEvent();
                event.setBooking(bookingId);
                event.setStatus(String.valueOf(booking.getStatus()));
                event.setUser(booking.getUser());

                bookingStatusUpdatedKafkaTemplate.send("update-booking-status", updatedBooking.getId(), event);

                log.info("Kafka event sent for booking status update");
            } catch (KafkaException ex) {
                log.error("Failed to send Kafka event for booking status update", ex);
                throw new KafkaException("Failed to send Kafka event for booking status update");
            }

            log.info("Booking status updated successfully for ID: {}", bookingId);
            return modelMapper.map(updatedBooking, BookingDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while updating booking status", ex);
            throw new DataIntegrityViolation("Failed to update booking status due to database error");
        }
    }

    @Override
    @Transactional
    public BookingDto updatePaymentStatus(Long bookingId, String paymentStatus) {
        try {
            log.info("Updating payment status for booking ID: {} to {}", bookingId, paymentStatus);

            if (bookingId == null || bookingId <= 0) {
                throw new BadRequestException("Invalid booking ID");
            }
            if (paymentStatus == null || paymentStatus.isBlank()) {
                throw new BadRequestException("Payment status cannot be empty");
            }

            PaymentStatus paymentStatusEnum;
            try {
                paymentStatusEnum = PaymentStatus.valueOf(paymentStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid payment status: " + paymentStatus);
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFound("Booking not found with ID: " + bookingId));

            if (booking.getStatus() == Status.CANCELLED && paymentStatusEnum == PaymentStatus.PAID) {
                throw new IllegalState("Cannot mark cancelled booking as paid");
            }

            booking.setPaymentStatus(paymentStatusEnum);
            Booking updatedBooking = bookingRepository.save(booking);

            log.info("Payment status updated successfully for booking ID: {}", bookingId);
            return modelMapper.map(updatedBooking, BookingDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while updating payment status", ex);
            throw new DataIntegrityViolation("Failed to update payment status due to database error");
        }
    }
}