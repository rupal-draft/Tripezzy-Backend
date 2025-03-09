package com.tripezzy.admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DestinationDto {
    @NotBlank(message = "Destination name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Country is required")
    @Size(max = 50, message = "Country name must not exceed 50 characters")
    private String country;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    public @NotBlank(message = "Destination name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Destination name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String name) {
        this.name = name;
    }

    public @NotBlank(message = "Country is required") @Size(max = 50, message = "Country name must not exceed 50 characters") String getCountry() {
        return country;
    }

    public void setCountry(@NotBlank(message = "Country is required") @Size(max = 50, message = "Country name must not exceed 50 characters") String country) {
        this.country = country;
    }

    public @NotBlank(message = "Description is required") @Size(max = 1000, message = "Description must not exceed 1000 characters") String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "Description is required") @Size(max = 1000, message = "Description must not exceed 1000 characters") String description) {
        this.description = description;
    }
}
