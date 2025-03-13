package com.tripezzy.payment_service.controller;

import com.tripezzy.payment_service.dto.PaymentRequest;
import com.tripezzy.payment_service.dto.PaymentResponse;
import com.tripezzy.payment_service.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout/{productId}")
    public ResponseEntity<PaymentResponse> checkoutProducts(@RequestBody  PaymentRequest paymentRequest,
                                                            @PathVariable Long productId,
                                                            @RequestParam(required = true) Long userId) {
        return ResponseEntity.ok(paymentService.checkoutProducts(paymentRequest, productId, userId));
    }

    @PutMapping("/success")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestParam(required = true) String session_id) {
        return ResponseEntity.ok(paymentService.confirmPayment(session_id));
    }
}
