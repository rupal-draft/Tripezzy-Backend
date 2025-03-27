package com.tripezzy.blog_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "likes", indexes = {
        @Index(name = "idx_like_blog_id", columnList = "blog_id"),
        @Index(name = "idx_like_user_id", columnList = "user_id")
})
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id")
    private Long user;

    @NotNull(message = "Blog cannot be null")
    @ManyToOne
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Like() {
    }

    public Like(Long id, Long userId, Blog blog, LocalDateTime createdAt) {
        this.id = id;
        this.user = userId;
        this.blog = blog;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull(message = "User ID cannot be null") Long getUserId() {
        return user;
    }

    public void setUserId(@NotNull(message = "User ID cannot be null") Long userId) {
        this.user = userId;
    }

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
