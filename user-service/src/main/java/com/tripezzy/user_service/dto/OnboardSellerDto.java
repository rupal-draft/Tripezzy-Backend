package com.tripezzy.user_service.dto;

import jakarta.validation.constraints.*;

public class OnboardSellerDto {

    @NotBlank(message = "Business name is required")
    @Size(min = 2, message = "Business name must be at least 2 characters long")
    private String businessName;

    @NotBlank(message = "Business description is required")
    @Size(min = 2, max = 1000, message = "Business description must be at least 2 characters long and less than 1000 characters")
    private String businessDescription;

    @NotBlank(message = "Business address is required")
    @Size(min = 2, max = 100, message = "Business address must be at least 2 characters long and less than 100 characters")
    private String businessAddress;

    @NotBlank(message = "Business phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String contactNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 10, message = "Phone number cannot exceed 15 characters")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String phoneNumber;

    public @NotBlank(message = "Business name is required") @Size(min = 2, message = "Business name must be at least 2 characters long") String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(@NotBlank(message = "Business name is required") @Size(min = 2, message = "Business name must be at least 2 characters long") String businessName) {
        this.businessName = businessName;
    }

    public @NotBlank(message = "Business description is required") @Size(min = 2, max = 1000, message = "Business description must be at least 2 characters long and less than 1000 characters") String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(@NotBlank(message = "Business description is required") @Size(min = 2, max = 1000, message = "Business description must be at least 2 characters long and less than 1000 characters") String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public @NotBlank(message = "Business address is required") @Size(min = 2, max = 100, message = "Business address must be at least 2 characters long and less than 100 characters") String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(@NotBlank(message = "Business address is required") @Size(min = 2, max = 100, message = "Business address must be at least 2 characters long and less than 100 characters") String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public @NotBlank(message = "Business phone number is required") @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits") String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(@NotBlank(message = "Business phone number is required") @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits") String contactNumber) {
        this.contactNumber = contactNumber;
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

    public @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password) {
        this.password = password;
    }

    public @Size(max = 10, message = "Phone number cannot exceed 15 characters") @NotBlank(message = "Phone number is required") String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@Size(max = 10, message = "Phone number cannot exceed 15 characters") @NotBlank(message = "Phone number is required") String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
