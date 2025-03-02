package com.tripezzy.blog_service.service;

import com.tripezzy.blog_service.dto.BlogDto;
import com.tripezzy.blog_service.dto.BlogResponseDto;
import com.tripezzy.blog_service.dto.CommentDto;
import com.tripezzy.blog_service.dto.LikeDto;
import com.tripezzy.blog_service.entity.enums.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BlogService {
    BlogResponseDto createBlog(BlogDto blogDto);

    BlogResponseDto getBlogById(Long blogId);

    Page<BlogResponseDto> getAllBlogs(Pageable pageable);

    Page<BlogResponseDto> getBlogsByAuthorId(Long authorId, Pageable pageable);

    Page<BlogResponseDto> getBlogsByStatus(String status, Pageable pageable);

    BlogResponseDto updateBlog(Long blogId, BlogDto blogDto);

    void deleteBlog(Long blogId);

    LikeDto addLikeToBlog(Long blogId, LikeDto likeDto);

    void removeLikeFromBlog(Long blogId, Long likeId);

    CommentDto addCommentToBlog(Long blogId, CommentDto commentDto);

    CommentDto updateComment(Long blogId, Long commentId, CommentDto commentDto);

    void deleteComment(Long blogId, Long commentId);

    List<LikeDto> getLikesForBlog(Long blogId);

    List<CommentDto> getCommentsForBlog(Long blogId);

    Page<BlogResponseDto> searchBlogs(String query, Pageable pageable);

    Page<BlogResponseDto> filterBlogs(String category, String tags, Pageable pageable);

    BlogResponseDto updateBlogStatus(Long blogId, BlogStatus status);

    void softDeleteBlog(Long blogId);

    Page<BlogResponseDto> advanceFilterBlogs(
            Long authorId,
            BlogStatus status,
            String category,
            String tags,
            Pageable pageable);
}
