package com.tripezzy.payment_service.service.implementation;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.tripezzy.payment_service.dto.*;
import com.tripezzy.payment_service.entity.Payment;
import com.tripezzy.payment_service.entity.enums.PaymentCategory;
import com.tripezzy.payment_service.entity.enums.PaymentStatus;
import com.tripezzy.payment_service.repository.PaymentRepository;
import com.tripezzy.payment_service.service.PaymentService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    @Transactional
    public ResponseEcomPayment checkoutProducts(CartPaymentResponseDto paymentRequest, Long cartId, Long userId) {
        return processCheckout(paymentRequest.getName(), paymentRequest.getAmount(), paymentRequest.getQuantity(),
                cartId, userId, PaymentCategory.ECOM);
    }

    @Override
    @Transactional
    public ResponseBookingPayment checkoutBooking(BookingPaymentRequestDto paymentDetails, Long bookingId, Long userId) {
        ResponseEcomPayment response = processCheckout(paymentDetails.getName(), paymentDetails.getAmount(), 1L,
                bookingId, userId, PaymentCategory.BOOKING);
        return new ResponseBookingPayment.PaymentResponseBuilder()
                .status(response.getStatus())
                .message(response.getMessage())
                .sessionId(response.getSession())
                .sessionUrl(response.getSessionUrl())
                .amount(response.getAmount())
                .bookingId(bookingId)
                .build();
    }

    private ResponseEcomPayment processCheckout(String name, double amount, Long quantity, Long referenceId,
                                                Long userId, PaymentCategory category) {
        log.info("Processing checkout for reference ID: {}", referenceId);
        try {
            String currency = "USD";
            long amountInCents = Math.round(amount * 100);

            Session session = createStripeSession(name, currency, amountInCents, quantity);

            log.info("Stripe session created successfully: {}", session.getId());

            ResponseEcomPayment paymentResponse = new ResponseEcomPayment.PaymentResponseBuilder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .status(PaymentStatus.CONFIRMED)
                    .amount(amount)
                    .message("Your payment was successful! Thank you for your purchase.")
                    .currency(currency)
                    .productName(name)
                    .quantity(quantity)
                    .build();

            savePayment(paymentResponse, referenceId, userId, category);
            return paymentResponse;
        } catch (StripeException e) {
            log.error("Error creating Stripe session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Stripe checkout session");
        }
    }

    private Session createStripeSession(String name, String currency, long amountInCents, Long quantity) throws StripeException {
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(name)
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(currency)
                        .setUnitAmount(amountInCents)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(quantity)
                        .setPriceData(priceData)
                        .build();

        return Session.create(SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/api/v1/payments/core/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:8080/api/v1/payments/core/cancel")
                .addLineItem(lineItem)
                .addAllPaymentMethodType(List.of(
                        SessionCreateParams.PaymentMethodType.CARD,
                        SessionCreateParams.PaymentMethodType.AMAZON_PAY
                ))
                .build());
    }

    private void savePayment(ResponseEcomPayment paymentResponse, Long referenceId, Long userId, PaymentCategory category) {
        Payment payment = modelMapper.map(paymentResponse, Payment.class);
        payment.setUser(userId);
        payment.setReference(referenceId);
        payment.setCategory(category);
        payment.setStatus(PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);
    }

    @Override
    @Cacheable(value = "payments", key = "payments")
    public List<PaymentsResponse> getAllPayments() {

        log.info("Fetching all payments");
        List<Payment> payments = paymentRepository.findAll();

        return payments
                .stream()
                .map(payment -> modelMapper
                        .map(payment, PaymentsResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "payments", key = "#userId")
    public List<PaymentsResponse> getAllPaymentsByUserId(Long userId) {

        log.info("Fetching all payments for user {}", userId);
        List<Payment> payments = paymentRepository.findByUser(userId);

        return payments
                .stream()
                .map(payment -> modelMapper
                        .map(payment, PaymentsResponse.class))
                .collect(Collectors.toList());
    }
}

