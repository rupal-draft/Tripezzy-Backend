package com.tripezzy.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "sellers", indexes = {
        @Index(name = "idx_seller_user_id", columnList = "user_id")
})
public class Seller extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Business name is required")
    @Column(name = "business_name")
    @Size(min = 2, message = "Business name must be at least 2 characters long")
    private String businessName;

    @NotBlank(message = "Business description is required")
    @Column(name = "business_description", columnDefinition = "TEXT")
    @Size(min = 2,max = 1000, message = "Business description must be at least 2 characters long and less than 1000 characters")
    private String businessDescription;

    @NotBlank(message = "Business address is required")
    @Column(name = "business_address")
    @Size(min = 2, max = 100, message = "Business address must be at least 2 characters long and less than 100 characters")
    private String businessAddress;

    @NotBlank(message = "Business phone number is required")
    @Size(max = 10, message = "Phone number cannot exceed 15 characters")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    @Column(nullable = false, unique = true, name = "business_contact")
    private String contactNumber;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public @NotBlank(message = "Business phone number is required") @Size(max = 10, message = "Phone number cannot exceed 15 characters") String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(@NotBlank(message = "Business phone number is required") @Size(max = 10, message = "Phone number cannot exceed 15 characters") String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
