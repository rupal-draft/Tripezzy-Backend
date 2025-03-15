package com.tripezzy.payment_service.service.implementation;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.tripezzy.booking_service.grpc.BookingPaymentResponse;
import com.tripezzy.eCommerce_service.grpc.CartPaymentResponse;
import com.tripezzy.payment_service.dto.PaymentResponse;
import com.tripezzy.payment_service.dto.ResponseBookingPayment;
import com.tripezzy.payment_service.entity.Payment;
import com.tripezzy.payment_service.entity.enums.PaymentCategory;
import com.tripezzy.payment_service.entity.enums.PaymentStatus;
import com.tripezzy.payment_service.exception.ResourceNotFound;
import com.tripezzy.payment_service.repository.PaymentRepository;
import com.tripezzy.payment_service.service.PaymentService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public PaymentResponse checkoutProducts(CartPaymentResponse paymentRequest, Long cartId, Long userId) {
        return processCheckout(paymentRequest.getName(), paymentRequest.getAmount(), paymentRequest.getQuantity(),
                cartId, userId, PaymentCategory.ECOM);
    }

    @Override
    @Transactional
    public ResponseBookingPayment checkoutBooking(BookingPaymentResponse paymentDetails, Long bookingId, Long userId) {
        PaymentResponse response = processCheckout(paymentDetails.getName(), paymentDetails.getAmount(), 1L,
                bookingId, userId, PaymentCategory.BOOKING);
        return new ResponseBookingPayment.PaymentResponseBuilder()
                .status(response.getStatus())
                .message("Booking payment initiated successfully")
                .sessionId(response.getSessionId())
                .sessionUrl(response.getSessionUrl())
                .amount(response.getAmount())
                .bookingId(bookingId)
                .build();
    }

    private PaymentResponse processCheckout(String name, double amount, Long quantity, Long referenceId,
                                            Long userId, PaymentCategory category) {
        log.info("Processing checkout for reference ID: {}", referenceId);
        try {
            String currency = "USD";
            long amountInCents = Math.round(amount * 100);

            Session session = createStripeSession(name, currency, amountInCents, quantity);

            log.info("Stripe session created successfully: {}", session.getId());

            PaymentResponse paymentResponse = new PaymentResponse.PaymentResponseBuilder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .status(PaymentStatus.PENDING)
                    .amount(amount)
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
                .setSuccessUrl("http://localhost:4000/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:4000/payment/cancel")
                .addLineItem(lineItem)
                .addAllPaymentMethodType(List.of(
                        SessionCreateParams.PaymentMethodType.CARD,
                        SessionCreateParams.PaymentMethodType.AMAZON_PAY
                ))
                .build());
    }

    private void savePayment(PaymentResponse paymentResponse, Long referenceId, Long userId, PaymentCategory category) {
        Payment payment = modelMapper.map(paymentResponse, Payment.class);
        payment.setUserId(userId);
        payment.setReferenceId(referenceId);
        payment.setCategory(category);
        paymentRepository.save(payment);
    }

    @Override
    public PaymentResponse confirmPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFound("Payment not found", HttpStatus.NOT_FOUND));

        payment.setStatus(PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);

        return modelMapper.map(payment, PaymentResponse.class);
    }
}

