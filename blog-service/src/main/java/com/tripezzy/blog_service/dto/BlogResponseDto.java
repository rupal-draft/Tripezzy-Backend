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
}
