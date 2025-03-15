package com.tripezzy.payment_service.dto;

import com.tripezzy.payment_service.entity.enums.PaymentCategory;
import com.tripezzy.payment_service.entity.enums.PaymentStatus;


import java.time.LocalDateTime;

public class PaymentsResponse {
    private Long id;

    private Long userId;

    private Long referenceId ;

    private String sessionId;

    private PaymentStatus status;

    private Long amount;

    private String currency;

    private String name;

    private PaymentCategory category;

    private Long quantity ;

    private LocalDateTime createdAt;

    public PaymentsResponse(Long id, Long userId, Long referenceId, String sessionId, PaymentStatus status, Long amount, String currency, String name, PaymentCategory category, Long quantity, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.referenceId = referenceId;
        this.sessionId = sessionId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PaymentCategory getCategory() {
        return category;
    }

    public void setCategory(PaymentCategory category) {
        this.category = category;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
