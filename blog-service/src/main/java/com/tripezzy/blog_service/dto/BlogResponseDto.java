package com.tripezzy.blog_service.dto;

import com.tripezzy.blog_service.entity.enums.BlogStatus;

import java.time.LocalDateTime;
import java.util.List;

public class BlogResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private List<LikeDto> likes;
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

    public List<LikeDto> getLikes() {
        return likes;
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
}
