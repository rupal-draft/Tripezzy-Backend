package com.tripezzy.admin_service.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TourPackageDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 1, message = "Minimum capacity must be at least 1")
    private Integer capacity;

    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDateTime startDate;

    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @NotNull(message = "Destination ID is required")
    private Long destinationId;

    public @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String name) {
        this.name = name;
    }

    public @NotBlank(message = "Description is required") @Size(max = 1000, message = "Description must not exceed 1000 characters") String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "Description is required") @Size(max = 1000, message = "Description must not exceed 1000 characters") String description) {
        this.description = description;
    }

    public @Positive(message = "Price must be positive") BigDecimal getPrice() {
        return price;
    }

    public void setPrice(@Positive(message = "Price must be positive") BigDecimal price) {
        this.price = price;
    }

    public @Min(value = 1, message = "Minimum capacity must be at least 1") Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(@Min(value = 1, message = "Minimum capacity must be at least 1") Integer capacity) {
        this.capacity = capacity;
    }

    public @FutureOrPresent(message = "Start date cannot be in the past") LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(@FutureOrPresent(message = "Start date cannot be in the past") LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public @Future(message = "End date must be in the future") LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(@Future(message = "End date must be in the future") LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public @NotNull(message = "Destination ID is required") Long getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(@NotNull(message = "Destination ID is required") Long destinationId) {
        this.destinationId = destinationId;
    }
}
