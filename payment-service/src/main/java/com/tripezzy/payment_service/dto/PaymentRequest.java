package com.tripezzy.payment_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Long amount;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Currency is required")
    private String currency;

    public @NotNull(message = "Amount is required") @Min(value = 1, message = "Amount must be at least 1") Long getAmount() {
        return amount;
    }

    public void setAmount(@NotNull(message = "Amount is required") @Min(value = 1, message = "Amount must be at least 1") Long amount) {
        this.amount = amount;
    }

    public @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Long getQuantity() {
        return quantity;
    }

    public void setQuantity(@NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Long quantity) {
        this.quantity = quantity;
    }

    public @NotBlank(message = "Product name is required") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Product name is required") String name) {
        this.name = name;
    }

    public @NotBlank(message = "Currency is required") String getCurrency() {
        return currency;
    }

    public void setCurrency(@NotBlank(message = "Currency is required") String currency) {
        this.currency = currency;
    }
}
