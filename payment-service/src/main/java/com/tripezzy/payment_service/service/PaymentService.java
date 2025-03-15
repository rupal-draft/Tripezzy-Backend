package com.tripezzy.payment_service.service;

import com.tripezzy.booking_service.grpc.BookingPaymentResponse;
import com.tripezzy.eCommerce_service.grpc.CartPaymentResponse;
import com.tripezzy.payment_service.dto.PaymentResponse;
import com.tripezzy.payment_service.dto.ResponseBookingPayment;

public interface PaymentService {

    PaymentResponse checkoutProducts(CartPaymentResponse productRequest,Long cartId, Long userId);

    PaymentResponse confirmPayment(String sessionId);

    ResponseBookingPayment checkoutBooking(BookingPaymentResponse paymentDetails, Long bookingId, Long userId);
}
