package com.tripezzy.payment_service.service.implementation;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.tripezzy.payment_service.dto.PaymentRequest;
import com.tripezzy.payment_service.dto.PaymentResponse;
import com.tripezzy.payment_service.entity.Payment;
import com.tripezzy.payment_service.entity.enums.PaymentStatus;
import com.tripezzy.payment_service.exception.ResourceNotFound;
import com.tripezzy.payment_service.repository.PaymentRepository;
import com.tripezzy.payment_service.service.PaymentService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository, ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PaymentResponse checkoutProducts(PaymentRequest paymentRequest, Long productId, Long userId) {

        try {
            String currency = Optional.ofNullable(paymentRequest.getCurrency()).orElse("USD");
            long amountInCents = Optional.ofNullable(paymentRequest.getAmount()).orElse(0L) * 100;

            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(paymentRequest.getName())
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(currency)
                            .setUnitAmount(amountInCents)
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(paymentRequest.getQuantity())
                            .setPriceData(priceData)
                            .build();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:4000/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:4000/payment/cancel")
                    .addLineItem(lineItem)
                    .addAllPaymentMethodType(List.of(
                            SessionCreateParams.PaymentMethodType.CARD,
                            SessionCreateParams.PaymentMethodType.AMAZON_PAY
                    ))
                    .build();

            Session session = Session.create(params);

            log.info("Stripe session created successfully: {}", session.getId());

            PaymentResponse paymentResponse =  new PaymentResponse.PaymentResponseBuilder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .status(PaymentStatus.PENDING)
                    .amount(paymentRequest.getAmount())
                    .currency(currency)
                    .productName(paymentRequest.getName())
                    .quantity(paymentRequest.getQuantity())
                    .build();

            Payment payment = modelMapper.map(paymentResponse, Payment.class);
            payment.setUserId(userId);
            payment.setProductId(productId);
            paymentRepository.save(payment);
            return paymentResponse;

        } catch (StripeException e) {
            log.error("Error creating Stripe session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Stripe checkout session");
        }
    }

    @Override
    public PaymentResponse confirmPayment(String sessionId) {

        Payment payment = paymentRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFound("Payment not found", HttpStatus.NOT_FOUND));

        payment.setStatus(PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);

        return modelMapper.map(payment, PaymentResponse.class);
    }
}
