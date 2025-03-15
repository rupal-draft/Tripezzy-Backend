package com.tripezzy.payment_service.controller;

import com.tripezzy.booking_service.grpc.BookingPaymentResponse;
import com.tripezzy.eCommerce_service.grpc.CartPaymentResponse;
import com.tripezzy.payment_service.dto.PaymentsResponse;
import com.tripezzy.payment_service.dto.ResponseEcomPayment;
import com.tripezzy.payment_service.dto.ResponseBookingPayment;
import com.tripezzy.payment_service.grpc.client.BookingGrpcClient;
import com.tripezzy.payment_service.grpc.client.CartGrpcClient;
import com.tripezzy.payment_service.service.PaymentService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/checkout/{cartId}")
    @RateLimiter(name = "checkoutProducts", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<ResponseEcomPayment> checkoutProducts(@PathVariable Long cartId,
                                                                @RequestParam(required = true) Long userId) {
        CartPaymentResponse paymentDetails = cartGrpcClient.getPaymentDetails(cartId);
        return ResponseEntity.ok(paymentService.checkoutProducts(paymentDetails,cartId,userId));
    }

    @PostMapping("/checkout/{bookingId}")
    @RateLimiter(name = "checkoutBookings", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<ResponseBookingPayment> checkoutBookings(@PathVariable Long bookingId,
                                                                   @RequestParam(required = true) Long userId) {
        BookingPaymentResponse paymentDetails = bookingGrpcClient.getBookingPayment(bookingId);
        return ResponseEntity.ok(paymentService.checkoutBooking(paymentDetails,bookingId,userId));
    }

    @PutMapping("/success")
    public ResponseEntity<ResponseEcomPayment> confirmPayment(@RequestParam(required = true) String session_id) {
        return ResponseEntity.ok(paymentService.confirmPayment(session_id));
    }

    @GetMapping
    @RateLimiter(name = "paymentsRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<PaymentsResponse>> getAllPayments(){
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/user/{userId}")
    @RateLimiter(name = "userPaymentsRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<PaymentsResponse>> getAllPaymentsByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(paymentService.getAllPaymentsByUserId(userId));
    }

    public ResponseEntity<String> rateLimitFallback() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again later.");
    }
}
