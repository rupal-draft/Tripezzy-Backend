package com.tripezzy.payment_service.controller;

import com.tripezzy.booking_service.grpc.BookingPaymentResponse;
import com.tripezzy.eCommerce_service.grpc.CartPaymentResponse;
import com.tripezzy.payment_service.dto.PaymentResponse;
import com.tripezzy.payment_service.dto.ResponseBookingPayment;
import com.tripezzy.payment_service.grpc.BookingGrpcClient;
import com.tripezzy.payment_service.grpc.CartGrpcClient;
import com.tripezzy.payment_service.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PaymentResponse> checkoutProducts(@PathVariable Long cartId,
                                                            @RequestParam(required = true) Long userId) {
        CartPaymentResponse paymentDetails = cartGrpcClient.getPaymentDetails(cartId);
        return ResponseEntity.ok(paymentService.checkoutProducts(paymentDetails,cartId,userId));
    }

    @PostMapping("/checkout/{bookingId}")
    public ResponseEntity<ResponseBookingPayment> checkoutBookings(@PathVariable Long bookingId,
                                                                   @RequestParam(required = true) Long userId) {
        BookingPaymentResponse paymentDetails = bookingGrpcClient.getBookingPayment(bookingId);
        return ResponseEntity.ok(paymentService.checkoutBooking(paymentDetails,bookingId,userId));
    }

    @PutMapping("/success")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestParam(required = true) String session_id) {
        return ResponseEntity.ok(paymentService.confirmPayment(session_id));
    }
}
