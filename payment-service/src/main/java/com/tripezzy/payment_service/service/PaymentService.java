package com.tripezzy.payment_service.service;

import com.tripezzy.booking_service.grpc.BookingPaymentResponse;
import com.tripezzy.eCommerce_service.grpc.CartPaymentResponse;
import com.tripezzy.payment_service.dto.PaymentsResponse;
import com.tripezzy.payment_service.dto.ResponseEcomPayment;
import com.tripezzy.payment_service.dto.ResponseBookingPayment;

import java.util.List;

public interface PaymentService {

    ResponseEcomPayment checkoutProducts(CartPaymentResponse productRequest, Long cartId, Long userId);

    ResponseEcomPayment confirmPayment(String sessionId);

    ResponseBookingPayment checkoutBooking(BookingPaymentResponse paymentDetails, Long bookingId, Long userId);

    List<PaymentsResponse> getAllPayments();

    List<PaymentsResponse> getAllPaymentsByUserId(Long userId);
}
