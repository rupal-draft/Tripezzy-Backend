package com.tripezzy.booking_service.entity;

import com.tripezzy.booking_service.entity.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @Column(name = "user_id")
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Column(name = "destination_id")
    @NotNull(message = "Destination ID cannot be null")
    private Long destinationId;

    @Column(name = "booking_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @NotNull(message = "Booking date cannot be null")
    private LocalDateTime bookingDate;

    @Column(name = "travel_date")
    @NotNull(message = "Travel date cannot be null")
    private LocalDate travelDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    @NotNull(message = "Status cannot be null")
    private Status status = Status.PENDING;

    @Column(name = "total_price")
    @NotNull(message = "Total price cannot be null")
    @Positive(message = "Total price must be positive")
    private BigDecimal totalPrice;
}
