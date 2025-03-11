package com.tripezzy.payment_service.service;

import com.tripezzy.payment_service.dto.PaymentRequest;
import com.tripezzy.payment_service.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse checkoutProducts(PaymentRequest productRequest, Long productId, Long userId);

    PaymentResponse confirmPayment(String sessionId);
}
