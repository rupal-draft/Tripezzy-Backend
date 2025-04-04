package com.tripezzy.payment_service.dto;

import com.tripezzy.payment_service.entity.enums.PaymentStatus;

public class ResponseEcomPayment {
    private PaymentStatus status;
    private String message;
    private String session;
    private String sessionUrl;
    private double amount;
    private Long quantity;
    private String productName;
    private String currency;
    public PaymentStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getSession() {
        return session;
    }

    public String getSessionUrl() {
        return sessionUrl;
    }

    public double getAmount() {
        return amount;
    }

    public Long getQuantity() {
        return quantity;
    }

    public String getProductName() {
        return productName;
    }

    public String getCurrency() {
        return currency;
    }

    public ResponseEcomPayment(PaymentResponseBuilder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.session = builder.session;
        this.sessionUrl = builder.sessionUrl;
        this.amount = builder.amount;
        this.quantity = builder.quantity;
        this.productName = builder.productName;
        this.currency = builder.currency;
    }

    public static class PaymentResponseBuilder {
        private PaymentStatus status;
        private String message;
        private String session;
        private String sessionUrl;
        private double amount;
        private Long quantity;
        private String productName;
        private String currency;

        public PaymentResponseBuilder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public PaymentResponseBuilder quantity(Long quantity) {
            this.quantity = quantity;
            return this;
        }

        public PaymentResponseBuilder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public PaymentResponseBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public PaymentResponseBuilder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public PaymentResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public PaymentResponseBuilder sessionId(String sessionId) {
            this.session = sessionId;
            return this;
        }

        public PaymentResponseBuilder sessionUrl(String sessionUrl) {
            this.sessionUrl = sessionUrl;
            return this;
        }

        public ResponseEcomPayment build() {
            return new ResponseEcomPayment(this);
        }
    }
}
