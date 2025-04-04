package com.tripezzy.payment_service.controller;

import com.tripezzy.payment_service.annotations.RoleRequired;
import com.tripezzy.payment_service.dto.*;
import com.tripezzy.payment_service.entity.enums.PaymentStatus;
import com.tripezzy.payment_service.grpc.client.BookingGrpcClient;
import com.tripezzy.payment_service.grpc.client.CartGrpcClient;
import com.tripezzy.payment_service.service.PaymentService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/core")
public class PaymentController {

    private final PaymentService paymentService;
    private final CartGrpcClient cartGrpcClient;
    private final BookingGrpcClient bookingGrpcClient;

    public PaymentController(PaymentService paymentService, CartGrpcClient cartGrpcClient, BookingGrpcClient bookingGrpcClient) {
        this.paymentService = paymentService;
        this.cartGrpcClient = cartGrpcClient;
        this.bookingGrpcClient = bookingGrpcClient;
    }

    @PostMapping("/checkout/shop/{cartId}")
    @RateLimiter(name = "checkoutProducts", fallbackMethod = "checkoutProductsFallback")
    public ResponseEntity<ResponseEcomPayment> checkoutProducts(@PathVariable Long cartId,
                                                                @RequestParam(required = true) Long userId) {
        CartPaymentResponseDto paymentDetails = cartGrpcClient.getPaymentDetails(cartId);
        return new ResponseEntity<>(paymentService.checkoutProducts(paymentDetails,cartId,userId),HttpStatus.CREATED);
    }

    @PostMapping("/checkout/bookings/{bookingId}")
    @RateLimiter(name = "checkoutBookings", fallbackMethod = "checkoutBookingsFallback")
    public ResponseEntity<ResponseBookingPayment> checkoutBookings(@PathVariable Long bookingId,
                                                                   @RequestParam(required = true) Long userId) {
        BookingPaymentRequestDto paymentDetails = bookingGrpcClient.getBookingPayment(bookingId);
        return new ResponseEntity<>(paymentService.checkoutBooking(paymentDetails,bookingId,userId),HttpStatus.CREATED);
    }

    @GetMapping
    @RoleRequired("ADMIN")
    @RateLimiter(name = "paymentsRateLimiter", fallbackMethod = "paymentsRateLimiterFallback")
    public ResponseEntity<List<PaymentsResponse>> getAllPayments(){
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/user/{userId}")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "userPaymentsRateLimiter", fallbackMethod = "userPaymentsRateLimiterFallback")
    public ResponseEntity<List<PaymentsResponse>> getAllPaymentsByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(paymentService.getAllPaymentsByUserId(userId));
    }

    public ResponseEntity<ResponseEcomPayment> checkoutProductsFallback(Long cartId, Long userId, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ResponseEcomPayment.PaymentResponseBuilder()
                        .message("Too many checkout requests for products. Please try again in a few moments.")
                        .status(PaymentStatus.FAILED)
                        .build());
    }

    public ResponseEntity<ResponseBookingPayment> checkoutBookingsFallback(Long bookingId, Long userId, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ResponseBookingPayment.PaymentResponseBuilder()
                        .message("Too many booking checkout attempts. Please wait and try again shortly.")
                        .status(PaymentStatus.FAILED)
                        .build());
    }

    public ResponseEntity<List<PaymentsResponse>> paymentsRateLimiterFallback(Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Collections.emptyList());
    }

    public ResponseEntity<List<PaymentsResponse>> userPaymentsRateLimiterFallback(Long userId, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Collections.emptyList());
    }

}
