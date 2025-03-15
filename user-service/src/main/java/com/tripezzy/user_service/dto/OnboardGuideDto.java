package com.tripezzy.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OnboardGuideDto {

    @NotBlank(message = "Languages spoken are required")
    @Size(min = 2, max = 255, message = "Languages spoken must be between 2 and 255 characters")
    private String languagesSpoken;

    @NotBlank(message = "Experience details are required")
    @Size(min = 5, max = 1000, message = "Experience must be between 5 and 1000 characters")
    private String experience;

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

    public @NotBlank(message = "Languages spoken are required") @Size(min = 2, max = 255, message = "Languages spoken must be between 2 and 255 characters") String getLanguagesSpoken() {
        return languagesSpoken;
    }

    public void setLanguagesSpoken(@NotBlank(message = "Languages spoken are required") @Size(min = 2, max = 255, message = "Languages spoken must be between 2 and 255 characters") String languagesSpoken) {
        this.languagesSpoken = languagesSpoken;
    }

    public @NotBlank(message = "Experience details are required") @Size(min = 5, max = 1000, message = "Experience must be between 5 and 1000 characters") String getExperience() {
        return experience;
    }

    public void setExperience(@NotBlank(message = "Experience details are required") @Size(min = 5, max = 1000, message = "Experience must be between 5 and 1000 characters") String experience) {
        this.experience = experience;
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
