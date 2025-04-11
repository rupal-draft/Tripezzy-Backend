package com.tripezzy.blog_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public class BlogDto implements Serializable {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "Author ID cannot be null")
    private Long authorId;

    @NotBlank(message = "Category cannot be blank")
    private String category;

    @NotBlank(message = "Tags cannot be blank")
    private String tag;

    public BlogDto(String title, String content, Long authorId, String category, String tag) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.category = category;
        this.tag = tag;
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

    public @NotBlank(message = "Category cannot be blank") String getCategory() {
        return category;
    }

    public void setCategory(@NotBlank(message = "Category cannot be blank") String category) {
        this.category = category;
    }

    public @NotBlank(message = "Tags cannot be blank") String getTag() {
        return tag;
    }

    public void setTag(@NotBlank(message = "Tags cannot be blank") String tag) {
        this.tag = tag;
    }
}
