package com.tripezzy.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDto {
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 10, message = "Phone number cannot exceed 15 characters")
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "First name is required") @Size(max = 50, message = "First name cannot exceed 50 characters") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank(message = "First name is required") @Size(max = 50, message = "First name cannot exceed 50 characters") String firstName) {
        this.firstName = firstName;
    }

    public @NotBlank(message = "Last name is required") @Size(max = 50, message = "Last name cannot exceed 50 characters") String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank(message = "Last name is required") @Size(max = 50, message = "Last name cannot exceed 50 characters") String lastName) {
        this.lastName = lastName;
    }

    public @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public @Size(max = 10, message = "Phone number cannot exceed 15 characters") @NotBlank(message = "Phone number is required") String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@Size(max = 10, message = "Phone number cannot exceed 15 characters") @NotBlank(message = "Phone number is required") String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
