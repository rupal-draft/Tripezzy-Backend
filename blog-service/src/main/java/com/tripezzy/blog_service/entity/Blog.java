package com.tripezzy.blog_service.entity;

import com.tripezzy.blog_service.entity.enums.BlogStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "blogs", indexes = {
        @Index(name = "idx_blog_author_id", columnList = "author_id"),
        @Index(name = "idx_blog_status", columnList = "status"),
        @Index(name = "idx_blog_category", columnList = "category"),
        @Index(name = "idx_blog_tags", columnList = "tag")
})
@EntityListeners(AuditingEntityListener.class)
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "Author ID cannot be null")
    @Column(name = "author_id")
    private Long authorId;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    private BlogStatus status = BlogStatus.DRAFT;

    @NotBlank(message = "Category cannot be blank")
    private String category;

    @NotBlank(message = "Tags cannot be blank")
    private String tag;

    @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    public Blog() {
    }

    public Blog(Long id, String title, String content, Long authorId, List<Like> likes, List<Comment> comments, LocalDateTime createdAt, LocalDateTime updatedAt, BlogStatus status, String category, String tag, boolean deleted) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.likes = likes;
        this.comments = comments;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.category = category;
        this.tag = tag;
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Title cannot be blank") @Size(max = 200, message = "Title must be less than 200 characters") String getTitle() {
        return title;
    }

    public void setTitle(@NotBlank(message = "Title cannot be blank") @Size(max = 200, message = "Title must be less than 200 characters") String title) {
        this.title = title;
    }

    public @NotBlank(message = "Content cannot be blank") String getContent() {
        return content;
    }

    public void setContent(@NotBlank(message = "Content cannot be blank") String content) {
        this.content = content;
    }

    public @NotNull(message = "Author ID cannot be null") Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(@NotNull(message = "Author ID cannot be null") Long authorId) {
        this.authorId = authorId;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
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

    public @NotNull(message = "Status cannot be null") BlogStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull(message = "Status cannot be null") BlogStatus status) {
        this.status = status;
    }

    public @NotBlank(message = "Category cannot be blank") String getCategory() {
        return category;
    }

    public void setCategory(@NotBlank(message = "Category cannot be blank") String category) {
        this.category = category;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public @NotBlank(message = "Tags cannot be blank") String getTag() {
        return tag;
    }

    public void setTag(@NotBlank(message = "Tags cannot be blank") String tag) {
        this.tag = tag;
    }
}
