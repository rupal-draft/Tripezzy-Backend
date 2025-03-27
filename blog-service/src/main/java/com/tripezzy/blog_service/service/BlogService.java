package com.tripezzy.blog_service.service;

import com.tripezzy.blog_service.dto.BlogDto;
import com.tripezzy.blog_service.dto.BlogResponseDto;
import com.tripezzy.blog_service.dto.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BlogService {
    BlogResponseDto createBlog(BlogDto blogDto);

    BlogResponseDto getBlogById(Long blogId);

    Page<BlogResponseDto> getAllBlogs(Pageable pageable);

    Page<BlogResponseDto> getBlogsByAuthorId(Long authorId, Pageable pageable);

    Page<BlogResponseDto> getBlogsByTag(String tag, Pageable pageable);

    Page<BlogResponseDto> getBlogsByCategory(String category, Pageable pageable);

    BlogResponseDto updateBlog(Long blogId, BlogDto blogDto);

    void deleteBlog(Long blogId);

    void addLikeToBlog(Long blogId);

    void removeLikeFromBlog(Long blogId, Long likeId);

    CommentDto addCommentToBlog(Long blogId, CommentDto commentDto);

    void updateComment(Long blogId, Long commentId, String content);

    void deleteComment(Long blogId, Long commentId);

    Integer getLikesCountForBlog(Long blogId);

    List<CommentDto> getCommentsForBlog(Long blogId);

    Page<BlogResponseDto> searchBlogs(String query, Pageable pageable);

    Page<BlogResponseDto> filterBlogs(String category, String tags, Pageable pageable);

    BlogResponseDto updateBlogStatus(Long blogId, String status);

    void softDeleteBlog(Long blogId);

    Page<BlogResponseDto> advanceFilterBlogs(
            Long authorId,
            String category,
            String tags,
            Pageable pageable);

    Page<BlogResponseDto> getPublishedBlogs(Pageable pageable);
}
