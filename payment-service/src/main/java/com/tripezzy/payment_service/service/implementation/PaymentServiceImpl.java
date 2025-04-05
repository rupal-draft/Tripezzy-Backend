package com.tripezzy.payment_service.service.implementation;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.tripezzy.payment_service.dto.*;
import com.tripezzy.payment_service.entity.Payment;
import com.tripezzy.payment_service.entity.enums.PaymentCategory;
import com.tripezzy.payment_service.entity.enums.PaymentStatus;
import com.tripezzy.payment_service.exceptions.*;
import com.tripezzy.payment_service.repository.PaymentRepository;
import com.tripezzy.payment_service.service.PaymentService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
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
        try {
            log.info("Processing product checkout for cart ID: {} and user ID: {}", cartId, userId);

            // Validate input
            if (paymentRequest == null) {
                throw new BadRequestException("Payment request cannot be null");
            }
            if (cartId == null || cartId <= 0) {
                throw new BadRequestException("Invalid cart ID");
            }
            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID");
            }
            if (paymentRequest.getAmount() <= 0) {
                throw new BadRequestException("Amount must be greater than zero");
            }

            return processCheckout(paymentRequest.getName(), paymentRequest.getAmount(), paymentRequest.getQuantity(),
                    cartId, userId, PaymentCategory.ECOM);

        } catch (StripeException e) {
            log.error("Stripe error during product checkout: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to process payment with Stripe");
        } catch (DataAccessException e) {
            log.error("Database error during product checkout", e);
            throw new DataIntegrityViolation("Failed to save payment record");
        } catch (Exception e) {
            log.error("Unexpected error during product checkout", e);
            throw new ServiceUnavailable("Payment service is currently unavailable");
        }
    }

    @Override
    @Transactional
    public ResponseBookingPayment checkoutBooking(BookingPaymentRequestDto paymentDetails, Long bookingId, Long userId) {
        try {
            log.info("Processing booking checkout for booking ID: {} and user ID: {}", bookingId, userId);

            // Validate input
            if (paymentDetails == null) {
                throw new BadRequestException("Payment details cannot be null");
            }
            if (bookingId == null || bookingId <= 0) {
                throw new BadRequestException("Invalid booking ID");
            }
            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID");
            }
            if (paymentDetails.getAmount() <= 0) {
                throw new BadRequestException("Amount must be greater than zero");
            }

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

        } catch (StripeException e) {
            log.error("Stripe error during booking checkout: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to process payment with Stripe");
        } catch (DataAccessException e) {
            log.error("Database error during booking checkout", e);
            throw new DataIntegrityViolation("Failed to save payment record");
        } catch (Exception e) {
            log.error("Unexpected error during booking checkout", e);
            throw new ServiceUnavailable("Payment service is currently unavailable");
        }
    }

    private ResponseEcomPayment processCheckout(String name, double amount, Long quantity, Long referenceId,
                                                Long userId, PaymentCategory category) throws StripeException {
        log.info("Processing checkout for reference ID: {}", referenceId);

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
    }

    private Session createStripeSession(String name, String currency, long amountInCents, Long quantity)
            throws StripeException {
        try {
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
        } catch (StripeException e) {
            log.error("Stripe API error creating session", e);
            throw e;
        }
    }

    private void savePayment(ResponseEcomPayment paymentResponse, Long referenceId, Long userId, PaymentCategory category) {
        try {
            Payment payment = modelMapper.map(paymentResponse, Payment.class);
            payment.setUser(userId);
            payment.setReference(referenceId);
            payment.setCategory(category);
            payment.setStatus(PaymentStatus.CONFIRMED);
            paymentRepository.save(payment);
        } catch (DataAccessException e) {
            log.error("Failed to save payment record", e);
            throw new DataIntegrityViolation("Failed to save payment record");
        } catch (MappingException e) {
            log.error("Failed to map payment response", e);
            throw new IllegalState("Failed to process payment data");
        }
    }

    @Override
    @Cacheable(value = "payments", key = "'allPayments'")
    public List<PaymentsResponse> getAllPayments() {
        try {
            log.info("Fetching all payments");

            List<Payment> payments = paymentRepository.findAll();
            return payments.stream()
                    .map(payment -> {
                        try {
                            return modelMapper.map(payment, PaymentsResponse.class);
                        } catch (MappingException e) {
                            log.error("Mapping error for payment ID: {}", payment.getId(), e);
                            throw new IllegalState("Failed to map payment data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException e) {
            log.error("Database error while fetching all payments", e);
            throw new ServiceUnavailable("Unable to retrieve payments at this time");
        }
    }

    @Override
    @Cacheable(value = "userPayments", key = "#userId")
    public List<PaymentsResponse> getAllPaymentsByUserId(Long userId) {
        try {
            log.info("Fetching payments for user ID: {}", userId);

            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID");
            }

            List<Payment> payments = paymentRepository.findByUser(userId);
            return payments.stream()
                    .map(payment -> {
                        try {
                            return modelMapper.map(payment, PaymentsResponse.class);
                        } catch (MappingException e) {
                            log.error("Mapping error for payment ID: {}", payment.getId(), e);
                            throw new IllegalState("Failed to map payment data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException e) {
            log.error("Database error while fetching payments for user ID: {}", userId, e);
            throw new ServiceUnavailable("Unable to retrieve payments at this time");
        }
    }
}

