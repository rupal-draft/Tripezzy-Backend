package com.tripezzy.blog_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommentDto {

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 500, message = "Content must be less than 500 characters")
    private String content;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Blog ID cannot be null")
    private Long blogId;

    public CommentDto(String content, Long userId, Long blogId) {
        this.content = content;
        this.userId = userId;
        this.blogId = blogId;
    }

    public @NotBlank(message = "Content cannot be blank") @Size(max = 500, message = "Content must be less than 500 characters") String getContent() {
        return content;
    }

    public void setContent(@NotBlank(message = "Content cannot be blank") @Size(max = 500, message = "Content must be less than 500 characters") String content) {
        this.content = content;
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
