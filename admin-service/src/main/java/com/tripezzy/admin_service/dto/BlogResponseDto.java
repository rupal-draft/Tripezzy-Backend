package com.tripezzy.admin_service.dto;

import java.io.Serializable;

public class BlogResponseDto implements Serializable {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String category;
    private String tag;

    public BlogResponseDto() {
    }

    public BlogResponseDto(Long id, String title, String content, Long authorId, String category, String tag) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.category = category;
        this.tag = tag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
