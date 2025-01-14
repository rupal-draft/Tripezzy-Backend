package com.tripezzy.booking_service.entity;

import com.tripezzy.booking_service.entity.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @Column(name = "first_name")
    @NotNull(message = "First name cannot be null")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Column(name = "last_name")
    @NotNull(message = "Last name cannot be null")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Column(name = "email")
    @Email(message = "Invalid email format")
    @NotNull(message = "Email cannot be null")
    private String email;

    @Column(name = "phone_number", unique = true)
    @NotNull(message = "Phone number cannot be null")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    @Pattern(regexp = "\\+?[0-9]+", message = "Phone number must contain only digits and optionally start with +")
    private String phoneNumber;

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
