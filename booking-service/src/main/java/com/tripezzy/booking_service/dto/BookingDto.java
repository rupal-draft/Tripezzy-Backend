package com.tripezzy.booking_service.dto;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDto implements Serializable {

    private Long id;

    @NotNull(message = "First name cannot be null")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotNull(message = "Email cannot be null")
    private String email;

    @NotNull(message = "Phone number cannot be null")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    @Pattern(regexp = "\\+?[0-9]+", message = "Phone number must contain only digits and optionally start with +")
    private String phoneNumber;

    @NotNull(message = "User ID cannot be null")
    private Long user;

    @NotNull(message = "Destination ID cannot be null")
    private Long destination;

    @NotNull(message = "Travel date cannot be null")
    @FutureOrPresent(message = "Travel date must be in the present or future")
    private LocalDate travelDate;

    @NotNull(message = "Total price cannot be null")
    @Positive(message = "Total price must be positive")
    private BigDecimal totalPrice;


    public BookingDto() {
    }

    public BookingDto(Long bookingId, String firstName, String lastName, String email, String phoneNumber, Long userId, Long destinationId, LocalDate travelDate, BigDecimal totalPrice) {
        this.id = bookingId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.user = userId;
        this.destination = destinationId;
        this.travelDate = travelDate;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters

    public @NotNull(message = "First name cannot be null") @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters") @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes") String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull(message = "User ID cannot be null") Long getUser() {
        return user;
    }

    public void setUser(@NotNull(message = "User ID cannot be null") Long user) {
        this.user = user;
    }

    public @NotNull(message = "Destination ID cannot be null") Long getDestination() {
        return destination;
    }

    public void setDestination(@NotNull(message = "Destination ID cannot be null") Long destination) {
        this.destination = destination;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

}