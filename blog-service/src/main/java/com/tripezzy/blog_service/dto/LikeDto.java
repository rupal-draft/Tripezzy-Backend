package com.tripezzy.blog_service.dto;

import jakarta.validation.constraints.NotNull;

public class LikeDto {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Blog ID cannot be null")
    private Long blogId;

    public LikeDto(Long userId, Long blogId) {
        this.userId = userId;
        this.blogId = blogId;
    }

    public @NotNull(message = "User ID cannot be null") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID cannot be null") Long userId) {
        this.userId = userId;
    }

    public @NotNull(message = "Blog ID cannot be null") Long getBlogId() {
        return blogId;
    }

    public void setBlogId(@NotNull(message = "Blog ID cannot be null") Long blogId) {
        this.blogId = blogId;
    }
}
