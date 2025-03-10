package com.tripezzy.user_service.entity;

import com.tripezzy.user_service.entity.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users",indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role")
})
public class User extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 10, message = "Phone number cannot exceed 15 characters")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    @Column(nullable = false, unique = true, length = 10)
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Seller sellerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Guide guideProfile;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDeleted = false;

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

    public @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password) {
        this.password = password;
    }

    public @NotNull(message = "Role is required") UserRole getRole() {
        return role;
    }

    public void setRole(@NotNull(message = "Role is required") UserRole role) {
        this.role = role;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public @Size(max = 10, message = "Phone number cannot exceed 15 characters") @NotBlank(message = "Phone number is required") String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@Size(max = 10, message = "Phone number cannot exceed 15 characters") @NotBlank(message = "Phone number is required") String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Seller getSellerProfile() {
        return sellerProfile;
    }

    public void setSellerProfile(Seller sellerProfile) {
        this.sellerProfile = sellerProfile;
    }

    public Guide getGuideProfile() {
        return guideProfile;
    }

    public void setGuideProfile(Guide guideProfile) {
        this.guideProfile = guideProfile;
    }
}
