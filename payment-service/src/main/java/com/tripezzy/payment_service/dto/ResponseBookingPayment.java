package com.tripezzy.payment_service.dto;

import com.tripezzy.payment_service.entity.enums.PaymentStatus;

public class ResponseBookingPayment {
    private PaymentStatus status;
    private String message;
    private String sessionId;
    private String sessionUrl;
    private double amount;
    private Long bookingId;
    public PaymentStatus getStatus() {
        return status;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSessionUrl() {
        return sessionUrl;
    }

    public double getAmount() {
        return amount;
    }

    public ResponseBookingPayment(ResponseBookingPayment.PaymentResponseBuilder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.sessionId = builder.sessionId;
        this.sessionUrl = builder.sessionUrl;
        this.amount = builder.amount;
        this.bookingId = builder.bookingId;
    }

    public static class PaymentResponseBuilder {
        private PaymentStatus status;
        private String message;
        private String sessionId;
        private String sessionUrl;
        private double amount;
        private Long bookingId;

        public ResponseBookingPayment.PaymentResponseBuilder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public ResponseBookingPayment.PaymentResponseBuilder bookingId(Long bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public ResponseBookingPayment.PaymentResponseBuilder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public ResponseBookingPayment.PaymentResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ResponseBookingPayment.PaymentResponseBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public ResponseBookingPayment.PaymentResponseBuilder sessionUrl(String sessionUrl) {
            this.sessionUrl = sessionUrl;
            return this;
        }

        public ResponseBookingPayment build() {
            return new ResponseBookingPayment(this);
        }
    }
}
