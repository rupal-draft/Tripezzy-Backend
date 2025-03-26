package com.tripezzy.booking_service.controllers;

import com.tripezzy.booking_service.advices.ApiResponse;
import com.tripezzy.booking_service.annotations.RoleRequired;
import com.tripezzy.booking_service.auth.UserContext;
import com.tripezzy.booking_service.auth.UserContextHolder;
import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.entity.enums.Status;
import com.tripezzy.booking_service.service.BookingService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/core")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @RateLimiter(name = "createBookingRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("USER")
    public ResponseEntity<BookingDto> createBooking(
            @RequestBody BookingDto bookingDto,
            @RequestParam Long destinationId) {
        BookingDto savedBooking = bookingService.createBooking(bookingDto, destinationId);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    @GetMapping("/public/{bookingId}")
    @RateLimiter(name = "getBookingByIdRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long bookingId) {
        BookingDto booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/public")
    @RateLimiter(name = "defaultRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<BookingDto> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/public/paginated")
    @RateLimiter(name = "paginatedRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<BookingDto>> getAllBookings(Pageable pageable) {
        Page<BookingDto> bookings = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my-bookings")
    @RateLimiter(name = "userBookingsRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("USER")
    public ResponseEntity<Page<BookingDto>> getMyBookings(
            Pageable pageable) {
        UserContext userContext = UserContextHolder.getUserDetails();
        Long userId = userContext.getUserId();
        Page<BookingDto> bookings = bookingService.getBookingsByUserId(userId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{userId}")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "userBookingsRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<BookingDto>> getBookingsByUserId(@PathVariable Long userId,
            Pageable pageable) {
        Page<BookingDto> bookings = bookingService.getBookingsByUserId(userId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/public/destination/{destinationId}")
    @RateLimiter(name = "destinationBookingsRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<Page<BookingDto>> getBookingsByDestinationId(
            @PathVariable Long destinationId,
            Pageable pageable) {
        Page<BookingDto> bookings = bookingService.getBookingsByDestinationId(destinationId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping
    @RateLimiter(name = "statusBookingsRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<Page<BookingDto>> getBookingsByStatus(
            @RequestParam String status,
            Pageable pageable) {
        Page<BookingDto> bookings = bookingService.getBookingsByStatus(status, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/payment-status-price-range")
    @RateLimiter(name = "paymentStatusPriceRangeRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<BookingDto>> getBookingsByPaymentStatusAndPriceRange(
            @RequestParam String paymentStatus,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<BookingDto> bookings = bookingService.getBookingsByPaymentStatusAndPriceRange(paymentStatus, minPrice, maxPrice);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/upcoming/{status}")
    @RateLimiter(name = "upcomingBookingsRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<BookingDto>> getUpcomingBookingsByStatus(
            @PathVariable Status status) {
        List<BookingDto> bookings = bookingService.getUpcomingBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/count-by-status/{status}")
    @RateLimiter(name = "countByStatusRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<Long> countBookingsByStatus(@PathVariable Status status) {
        long count = bookingService.countBookingsByStatus(status);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{bookingId}")
    @RateLimiter(name = "deleteRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<ApiResponse<String>> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking canceled successfully"));
    }

    @DeleteMapping("/soft-delete/{bookingId}")
    @RateLimiter(name = "softDeleteRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<ApiResponse<String>> softDeleteBooking(@PathVariable Long bookingId) {
        bookingService.softDeleteBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking deleted successfully"));
    }

    @GetMapping("/payment-status/{paymentStatus}")
    @RateLimiter(name = "paymentStatusRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<BookingDto>> getBookingsByPaymentStatus(@PathVariable String paymentStatus) {
        List<BookingDto> bookings = bookingService.getBookingsByPaymentStatus(paymentStatus);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/{bookingId}/confirm")
    @RateLimiter(name = "confirmBookingRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<BookingDto> confirmBooking(@PathVariable Long bookingId) {
        BookingDto confirmedBooking = bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(confirmedBooking);
    }

    @GetMapping("/public/filter")
    @RateLimiter(name = "advancedFilterRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<BookingDto>> filterBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {
        Page<BookingDto> filteredBookings = bookingService.filterBookings(
                userId, destinationId, status, paymentStatus, minPrice, maxPrice, startDate, endDate, pageable);
        return ResponseEntity.ok(filteredBookings);
    }

    public ResponseEntity<String> rateLimitFallback() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again later.");
    }
}