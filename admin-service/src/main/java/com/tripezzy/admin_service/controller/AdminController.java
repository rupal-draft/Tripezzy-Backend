package com.tripezzy.admin_service.controller;

import com.tripezzy.admin_service.advices.ApiError;
import com.tripezzy.admin_service.advices.ApiResponse;
import com.tripezzy.admin_service.annotations.RoleRequired;
import com.tripezzy.admin_service.dto.BlogResponseDto;
import com.tripezzy.admin_service.dto.BookingDto;
import com.tripezzy.admin_service.dto.PaymentsResponseDto;
import com.tripezzy.admin_service.dto.ProductResponseDto;
import com.tripezzy.admin_service.grpc.BlogGrpcClient;
import com.tripezzy.admin_service.grpc.BookingGrpcClient;
import com.tripezzy.admin_service.grpc.PaymentGrpcClient;
import com.tripezzy.admin_service.grpc.ProductGrpcClient;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final BlogGrpcClient blogGrpcClient;
    private final BookingGrpcClient bookingGrpcClient;
    private final ProductGrpcClient productGrpcClient;
    private final PaymentGrpcClient paymentGrpcClient;

    public AdminController(BlogGrpcClient blogGrpcClient, BookingGrpcClient bookingGrpcClient, ProductGrpcClient productGrpcClient, PaymentGrpcClient paymentGrpcClient) {
        this.blogGrpcClient = blogGrpcClient;
        this.bookingGrpcClient = bookingGrpcClient;
        this.productGrpcClient = productGrpcClient;
        this.paymentGrpcClient = paymentGrpcClient;
    }

    @GetMapping("/blogs")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBlogsLimiter", fallbackMethod = "blogsRateLimitFallback")
    public ResponseEntity<List<BlogResponseDto>> getAllBlogs(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogGrpcClient.getAllBlogs(page, size));
    }

    public ResponseEntity<List<BlogResponseDto>> blogsRateLimitFallback(int page, int size, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
    }

    @GetMapping("/bookings/paginated")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBookingsLimiter", fallbackMethod = "bookingsRateLimitFallback")
    public ResponseEntity<List<BookingDto>> getAllBookings(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingGrpcClient.getAllBookings(page, size));
    }

    public ResponseEntity<List<BookingDto>> bookingsRateLimitFallback(int page, int size, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
    }

    @GetMapping("/bookings/user/{userId}")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBookingsByUserLimiter", fallbackMethod = "bookingsByUserRateLimitFallback")
    public ResponseEntity<List<BookingDto>> getBookingsByUserId(@PathVariable Long userId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingGrpcClient.getBookingsByUserId(userId, page, size));
    }

    public ResponseEntity<List<BookingDto>> bookingsByUserRateLimitFallback(Long userId, int page, int size, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
    }

    @GetMapping("/bookings/destination/{destinationId}")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBookingsByDestinationLimiter", fallbackMethod = "bookingsByDestinationRateLimitFallback")
    public ResponseEntity<List<BookingDto>> getBookingsByDestinationId(@PathVariable Long destinationId,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingGrpcClient.getBookingsByDestinationId(destinationId, page, size));
    }

    public ResponseEntity<List<BookingDto>> bookingsByDestinationRateLimitFallback(Long destinationId, int page, int size, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
    }

    @DeleteMapping("/bookings/soft-delete/{bookingId}")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBookingDeleteLimiter", fallbackMethod = "bookingDeleteRateLimitFallback")
    public ResponseEntity<ApiResponse<String>> softDeleteBooking(@PathVariable Long bookingId) {
        boolean success = bookingGrpcClient.softDeleteBooking(bookingId);
        return success
                ? ResponseEntity.ok(ApiResponse.success("Booking deleted successfully"))
                : ResponseEntity.ok(ApiResponse.error(new ApiError.ApiErrorBuilder()
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .setMessage("Booking failed to delete")
                .build()));
    }

    public ResponseEntity<ApiResponse<String>> bookingDeleteRateLimitFallback(Long bookingId, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(new ApiError.ApiErrorBuilder()
                        .setStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .setMessage("Too many requests. Please try again later.")
                        .build()));
    }

    @GetMapping("/bookings/payment-status")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBookingsByPaymentStatusLimiter", fallbackMethod = "bookingsByPaymentStatusRateLimitFallback")
    public ResponseEntity<List<BookingDto>> getBookingsByPaymentStatus(@RequestParam String paymentStatus) {
        return ResponseEntity.ok(bookingGrpcClient.getBookingsByPaymentStatus(paymentStatus));
    }

    public ResponseEntity<List<BookingDto>> bookingsByPaymentStatusRateLimitFallback(String paymentStatus, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
    }

    @PatchMapping("/bookings/{bookingId}/update/status")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBookingUpdateStatusLimiter", fallbackMethod = "bookingUpdateStatusRateLimitFallback")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long bookingId, @RequestParam String status) {
        return ResponseEntity.ok(bookingGrpcClient.updateBookingStatus(bookingId, status));
    }

    public ResponseEntity<BookingDto> bookingUpdateStatusRateLimitFallback(Long bookingId, String status, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(null);
    }

    @PatchMapping("/bookings/{bookingId}/update/payment-status")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBookingUpdatePaymentStatusLimiter", fallbackMethod = "bookingUpdatePaymentStatusRateLimitFallback")
    public ResponseEntity<BookingDto> updateBookingPaymentStatus(@PathVariable Long bookingId, @RequestParam String paymentStatus) {
        return ResponseEntity.ok(bookingGrpcClient.updateBookingPaymentStatus(bookingId, paymentStatus));
    }

    public ResponseEntity<BookingDto> bookingUpdatePaymentStatusRateLimitFallback(Long bookingId, String paymentStatus, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(null);
    }

    @GetMapping("/bookings/status")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBookingsByStatusLimiter", fallbackMethod = "bookingsByStatusRateLimitFallback")
    public ResponseEntity<List<BookingDto>> getBookingsByStatus(@RequestParam String status,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingGrpcClient.getBookingsByStatus(status, page, size));
    }

    public ResponseEntity<List<BookingDto>> bookingsByStatusRateLimitFallback(String status, int page, int size, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
    }

    @PatchMapping("/bookings/{bookingId}/confirm")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminBookingConfirmLimiter", fallbackMethod = "bookingConfirmRateLimitFallback")
    public ResponseEntity<BookingDto> confirmBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingGrpcClient.confirmBooking(bookingId));
    }

    public ResponseEntity<BookingDto> bookingConfirmRateLimitFallback(Long bookingId, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @GetMapping("/products")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminProductsLimiter", fallbackMethod = "productsRateLimitFallback")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productGrpcClient.getAllProducts(page, size));
    }

    public ResponseEntity<List<ProductResponseDto>> productsRateLimitFallback(int page, int size, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
    }

    @GetMapping("/payments")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminPaymentsLimiter", fallbackMethod = "paymentsRateLimitFallback")
    public ResponseEntity<List<PaymentsResponseDto>> getAllPayments() {
        return ResponseEntity.ok(paymentGrpcClient.getAllPayments());
    }

    public ResponseEntity<List<PaymentsResponseDto>> paymentsRateLimitFallback(Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
    }

    @GetMapping("/payments/{userId}")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "adminPaymentsByUserLimiter", fallbackMethod = "paymentsByUserRateLimitFallback")
    public ResponseEntity<List<PaymentsResponseDto>> getAllPaymentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentGrpcClient.getAllPaymentsByUserId(userId));
    }

    public ResponseEntity<List<PaymentsResponseDto>> paymentsByUserRateLimitFallback(Long userId, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
    }
}

