package com.tripezzy.user_service.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable {

    @CreatedDate
    @NotNull(message = "Creation date cannot be null")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @NotNull(message = "Update date cannot be null")
    private LocalDateTime updatedAt;

    public @NotNull(message = "Update date cannot be null") LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@NotNull(message = "Update date cannot be null") LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public @NotNull(message = "Creation date cannot be null") LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NotNull(message = "Creation date cannot be null") LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
