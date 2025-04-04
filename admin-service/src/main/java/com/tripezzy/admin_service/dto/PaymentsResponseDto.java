package com.tripezzy.admin_service.dto;

public class PaymentsResponseDto {
    private Long id;
    private Long userId;
    private Long referenceId;
    private String sessionId;
    private String status;
    private Long amount;
    private String currency;
    private String name;
    private String category;
    private Long quantity;
    private String createdAt;

    public PaymentsResponseDto() {
    }

    public PaymentsResponseDto(Long id, Long userId, Long referenceId, String sessionId, String status, Long amount, String currency, String name, String category, Long quantity, String createdAt) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
