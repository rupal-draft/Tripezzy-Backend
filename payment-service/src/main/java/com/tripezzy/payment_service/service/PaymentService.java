package com.tripezzy.payment_service.service;

import com.tripezzy.payment_service.dto.*;

import java.util.List;

public interface PaymentService {

    ResponseEcomPayment checkoutProducts(CartPaymentResponseDto productRequest, Long cartId, Long userId);

    ResponseBookingPayment checkoutBooking(BookingPaymentRequestDto paymentDetails, Long bookingId, Long userId);

    List<PaymentsResponse> getAllPayments();

    List<PaymentsResponse> getAllPaymentsByUserId(Long userId);
}
