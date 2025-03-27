package com.tripezzy.blog_service.dto;

import com.tripezzy.blog_service.entity.enums.BlogStatus;

import java.time.LocalDateTime;
import java.util.List;

public class BlogResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private List<CommentDto> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BlogStatus status;
    private String category;
    private String tag;


    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getAuthorId() {
        return authorId;
    }



    public List<CommentDto> getComments() {
        return comments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BlogStatus getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public String getTag() {
        return tag;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public void setComments(List<CommentDto> comments) {
        this.comments = comments;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setStatus(BlogStatus status) {
        this.status = status;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
