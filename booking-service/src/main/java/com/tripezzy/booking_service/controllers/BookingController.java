package com.tripezzy.booking_service.controllers;

import com.tripezzy.booking_service.advices.ApiError;
import com.tripezzy.booking_service.advices.ApiResponse;
import com.tripezzy.booking_service.annotations.RoleRequired;
import com.tripezzy.booking_service.auth.UserContext;
import com.tripezzy.booking_service.auth.UserContextHolder;
import com.tripezzy.booking_service.dto.BookingDto;
import com.tripezzy.booking_service.service.BookingService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/core")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @RateLimiter(name = "createBookingRateLimiter", fallbackMethod = "createBookingRateLimitFallback")
    @RoleRequired("USER")
    public ResponseEntity<BookingDto> createBooking(
            @RequestBody BookingDto bookingDto,
            @RequestParam Long destinationId) {
        BookingDto savedBooking = bookingService.createBooking(bookingDto, destinationId);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    @GetMapping("/public/{bookingId}")
    @RateLimiter(name = "getBookingByIdRateLimiter", fallbackMethod = "getBookingByIdRateLimitFallback")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long bookingId) {
        BookingDto booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/public")
    @RateLimiter(name = "defaultRateLimiter", fallbackMethod = "getAllBookingsRateLimitFallback")
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<BookingDto> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/public/paginated")
    @RateLimiter(name = "paginatedRateLimiter", fallbackMethod = "getAllBookingsPaginatedRateLimitFallback")
    public ResponseEntity<Page<BookingDto>> getAllBookings(Pageable pageable) {
        Page<BookingDto> bookings = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my-bookings")
    @RateLimiter(name = "userBookingsRateLimiter", fallbackMethod = "getMyBookingsRateLimitFallback")
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
    @RateLimiter(name = "userBookingsRateLimiter", fallbackMethod = "getBookingsByUserIdRateLimitFallback")
    public ResponseEntity<Page<BookingDto>> getBookingsByUserId(@PathVariable Long userId,
            Pageable pageable) {
        Page<BookingDto> bookings = bookingService.getBookingsByUserId(userId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/public/destination/{destinationId}")
    @RateLimiter(name = "destinationBookingsRateLimiter", fallbackMethod = "getBookingsByDestinationIdRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<Page<BookingDto>> getBookingsByDestinationId(
            @PathVariable Long destinationId,
            Pageable pageable) {
        Page<BookingDto> bookings = bookingService.getBookingsByDestinationId(destinationId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping
    @RateLimiter(name = "statusBookingsRateLimiter", fallbackMethod = "getBookingsByStatusRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<Page<BookingDto>> getBookingsByStatus(
            @RequestParam String status,
            Pageable pageable) {
        Page<BookingDto> bookings = bookingService.getBookingsByStatus(status, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/payment-status-price-range")
    @RateLimiter(name = "paymentStatusPriceRangeRateLimiter", fallbackMethod = "getBookingsByPaymentStatusAndPriceRangeRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<BookingDto>> getBookingsByPaymentStatusAndPriceRange(
            @RequestParam String paymentStatus,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<BookingDto> bookings = bookingService.getBookingsByPaymentStatusAndPriceRange(paymentStatus, minPrice, maxPrice);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/upcoming")
    @RateLimiter(name = "upcomingBookingsRateLimiter", fallbackMethod = "getUpcomingBookingsByStatusRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<BookingDto>> getUpcomingBookingsByStatus(
            @RequestParam String status) {
        List<BookingDto> bookings = bookingService.getUpcomingBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/count-by-status")
    @RateLimiter(name = "countByStatusRateLimiter", fallbackMethod = "countBookingsByStatusRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<ApiResponse<Long>> countBookingsByStatus(@RequestParam String status) {
        long count = bookingService.countBookingsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @DeleteMapping("/soft-delete/{bookingId}")
    @RateLimiter(name = "softDeleteRateLimiter", fallbackMethod = "softDeleteBookingRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<ApiResponse<String>> softDeleteBooking(@PathVariable Long bookingId) {
        bookingService.softDeleteBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking deleted successfully"));
    }

    @GetMapping("/payment-status")
    @RateLimiter(name = "paymentStatusRateLimiter", fallbackMethod = "getBookingsByPaymentStatusRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<BookingDto>> getBookingsByPaymentStatus(@RequestParam String paymentStatus) {
        List<BookingDto> bookings = bookingService.getBookingsByPaymentStatus(paymentStatus);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/public/filter")
    @RateLimiter(name = "advancedFilterRateLimiter", fallbackMethod = "filterBookingsRateLimitFallback")
    public ResponseEntity<Page<BookingDto>> filterBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        Page<BookingDto> filteredBookings = bookingService.filterBookings(
                userId, destinationId, status, paymentStatus, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(filteredBookings);
    }

    private ResponseEntity<ApiResponse<String>> rateLimitFallback(String serviceName, Throwable throwable) {
        ApiError apiError = new ApiError
                .ApiErrorBuilder()
                .setMessage("Too many requests to " + serviceName + ". Please try again later.")
                .setStatus(HttpStatus.TOO_MANY_REQUESTS)
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(apiError));
    }

    public ResponseEntity<ApiResponse<String>> createBookingRateLimitFallback(BookingDto bookingDto, Long destinationId, Throwable throwable) {
        return rateLimitFallback("createBooking", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBookingByIdRateLimitFallback(Long bookingId, Throwable throwable) {
        return rateLimitFallback("getBookingById", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getAllBookingsRateLimitFallback(Throwable throwable) {
        return rateLimitFallback("getAllBookings", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getAllBookingsPaginatedRateLimitFallback(Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getAllBookingsPaginated", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getMyBookingsRateLimitFallback(Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getMyBookings", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBookingsByUserIdRateLimitFallback(Long userId, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getBookingsByUserId", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBookingsByDestinationIdRateLimitFallback(Long destinationId, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getBookingsByDestinationId", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBookingsByStatusRateLimitFallback(String status, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getBookingsByStatus", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBookingsByPaymentStatusAndPriceRangeRateLimitFallback(String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice, Throwable throwable) {
        return rateLimitFallback("getBookingsByPaymentStatusAndPriceRange", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getUpcomingBookingsByStatusRateLimitFallback(String status, Throwable throwable) {
        return rateLimitFallback("getUpcomingBookingsByStatus", throwable);
    }

    public ResponseEntity<ApiResponse<String>> countBookingsByStatusRateLimitFallback(String status, Throwable throwable) {
        return rateLimitFallback("countBookingsByStatus", throwable);
    }

    public ResponseEntity<ApiResponse<String>> softDeleteBookingRateLimitFallback(Long bookingId, Throwable throwable) {
        return rateLimitFallback("softDeleteBooking", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBookingsByPaymentStatusRateLimitFallback(String paymentStatus, Throwable throwable) {
        return rateLimitFallback("getBookingsByPaymentStatus", throwable);
    }

    public ResponseEntity<ApiResponse<String>> filterBookingsRateLimitFallback(Long userId, Long destinationId, String status, String paymentStatus, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("filterBookings", throwable);
    }

}