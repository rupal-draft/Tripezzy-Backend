package com.tripezzy.admin_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour_packages", indexes = {
        @Index(name = "idx_tour_package_name", columnList = "name"),
        @Index(name = "idx_tour_package_price", columnList = "price")
})
public class TourPackage extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, length = 1000)
    private String description;

    @Positive(message = "Price must be positive")
    @Column(nullable = false)
    private BigDecimal price;

    @Min(value = 1, message = "Minimum capacity must be at least 1")
    @Column(nullable = false)
    private Integer capacity;

    @FutureOrPresent(message = "Start date cannot be in the past")
    @Column(nullable = false)
    private LocalDateTime startDate;

    @Future(message = "End date must be in the future")
    @Column(nullable = false)
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Name is required") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Name is required") String name) {
        this.name = name;
    }

    public @NotBlank(message = "Description is required") String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "Description is required") String description) {
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

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}
