package com.tripezzy.booking_service.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class PaymentInformationDto {

    private Long paymentId;

    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;

    @NotNull(message = "Card name cannot be null")
    @Size(min = 1, max = 50, message = "Card name must be between 1 and 50 characters")
    private String cardName;

    @NotNull(message = "Card number cannot be null")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    @Pattern(regexp = "[0-9]+", message = "Card number must contain only digits")
    private String cardNumber;

    @NotNull(message = "Expiration date cannot be null")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    @NotNull(message = "CVV cannot be null")
    @Min(value = 100, message = "CVV must be at least 3 digits")
    @Max(value = 999, message = "CVV must be at most 3 digits")
    private Long cvv;

    public PaymentInformationDto() {
    }

    public PaymentInformationDto(Long paymentId, Long bookingId, String cardName, String cardNumber, LocalDate expirationDate, Long cvv) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.cardName = cardName;
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
    }

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getCvv() {
        return cvv;
    }

    public void setCvv(Long cvv) {
        this.cvv = cvv;
    }
}