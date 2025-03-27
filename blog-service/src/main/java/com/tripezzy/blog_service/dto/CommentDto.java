package com.tripezzy.blog_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentDto {

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 500, message = "Content must be less than 500 characters")
    private String content;

    public CommentDto() {}

    public CommentDto(String content) {
        this.content = content;
    }

    public @NotBlank(message = "Content cannot be blank") @Size(max = 500, message = "Content must be less than 500 characters") String getContent() {
        return content;
    }

    public void setContent(@NotBlank(message = "Content cannot be blank") @Size(max = 500, message = "Content must be less than 500 characters") String content) {
        this.content = content;
    }

}
