package com.tripezzy.eCommerce_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 200, message = "Name must be less than 200 characters")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Stock cannot be null")
    @PositiveOrZero(message = "Stock must be positive or zero")
    private Integer stock;

    @NotBlank(message = "Category cannot be blank")
    private String category;

    @NotBlank(message = "Image URL cannot be blank")
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;


    public Product(Long id, String name, String description, Double price, Integer stock, String category, String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Name cannot be blank") @Size(max = 200, message = "Name must be less than 200 characters") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Name cannot be blank") @Size(max = 200, message = "Name must be less than 200 characters") String name) {
        this.name = name;
    }

    public @NotBlank(message = "Description cannot be blank") String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "Description cannot be blank") String description) {
        this.description = description;
    }

    public @NotNull(message = "Price cannot be null") @Positive(message = "Price must be positive") Double getPrice() {
        return price;
    }

    public void setPrice(@NotNull(message = "Price cannot be null") @Positive(message = "Price must be positive") Double price) {
        this.price = price;
    }

    public @NotNull(message = "Stock cannot be null") @PositiveOrZero(message = "Stock must be positive or zero") Integer getStock() {
        return stock;
    }

    public void setStock(@NotNull(message = "Stock cannot be null") @PositiveOrZero(message = "Stock must be positive or zero") Integer stock) {
        this.stock = stock;
    }

    public @NotBlank(message = "Category cannot be blank") String getCategory() {
        return category;
    }

    public void setCategory(@NotBlank(message = "Category cannot be blank") String category) {
        this.category = category;
    }

    public @NotBlank(message = "Image URL cannot be blank") String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@NotBlank(message = "Image URL cannot be blank") String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
