package com.tripezzy.payment_service.dto;

public class BookingPaymentRequestDto {
    private double amount;
    private long quantity;
    private String name;
    private String currency;

    public BookingPaymentRequestDto() {
    }

    public BookingPaymentRequestDto(double amount, long quantity, String name, String currency) {
        this.amount = amount;
        this.quantity = quantity;
        this.name = name;
        this.currency = currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
