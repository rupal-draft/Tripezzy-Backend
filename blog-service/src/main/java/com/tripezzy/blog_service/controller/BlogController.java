package com.tripezzy.blog_service.controller;

import com.tripezzy.blog_service.dto.BlogDto;
import com.tripezzy.blog_service.dto.BlogResponseDto;
import com.tripezzy.blog_service.dto.CommentDto;
import com.tripezzy.blog_service.dto.LikeDto;
import com.tripezzy.blog_service.entity.enums.BlogStatus;
import com.tripezzy.blog_service.exceptions.RuntimeConflict;
import com.tripezzy.blog_service.service.BlogService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/core")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @PostMapping
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<BlogResponseDto> createBlog(@RequestBody BlogDto blogDto) {
        BlogResponseDto savedBlog = blogService.createBlog(blogDto);
        return new ResponseEntity<>(savedBlog, HttpStatus.CREATED);
    }

    @GetMapping("/{blogId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<BlogResponseDto> getBlogById(@PathVariable Long blogId) {
        BlogResponseDto blog = blogService.getBlogById(blogId);
        return ResponseEntity.ok(blog);
    }

    @GetMapping
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> getAllBlogs(Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.getAllBlogs(pageable);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/author/{authorId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> getBlogsByAuthorId(
            @PathVariable Long authorId,
            Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.getBlogsByAuthorId(authorId, pageable);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/status/{status}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> getBlogsByStatus(
            @PathVariable String status,
            Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.getBlogsByStatus(status, pageable);
        return ResponseEntity.ok(blogs);
    }

    @PutMapping("/{blogId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<BlogResponseDto> updateBlog(
            @PathVariable Long blogId,
            @RequestBody BlogDto blogDto) {
        BlogResponseDto updatedBlog = blogService.updateBlog(blogId, blogDto);
        return ResponseEntity.ok(updatedBlog);
    }

    @PutMapping("/{blogId}/status/{status}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<BlogResponseDto> updateBlogStatus(
            @PathVariable Long blogId,
            @PathVariable BlogStatus status) {
        BlogResponseDto updatedBlog = blogService.updateBlogStatus(blogId, status);
        return ResponseEntity.ok(updatedBlog);
    }

    @DeleteMapping("/{blogId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long blogId) {
        blogService.deleteBlog(blogId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{blogId}/likes")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<LikeDto> addLikeToBlog(
            @PathVariable Long blogId,
            @RequestBody LikeDto likeDto) {
        LikeDto savedLike = blogService.addLikeToBlog(blogId, likeDto);
        return new ResponseEntity<>(savedLike, HttpStatus.CREATED);
    }

    @DeleteMapping("/{blogId}/likes/{likeId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Void> removeLikeFromBlog(
            @PathVariable Long blogId,
            @PathVariable Long likeId) {
        blogService.removeLikeFromBlog(blogId, likeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{blogId}/comments")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<CommentDto> addCommentToBlog(
            @PathVariable Long blogId,
            @RequestBody CommentDto commentDto) {
        CommentDto savedComment = blogService.addCommentToBlog(blogId, commentDto);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    @PutMapping("/{blogId}/comments/{commentId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @RequestBody CommentDto commentDto) {
        CommentDto updatedComment = blogService.updateComment(blogId, commentId, commentDto);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{blogId}/comments/{commentId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId) {
        blogService.deleteComment(blogId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{blogId}/likes")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<LikeDto>> getLikesForBlog(@PathVariable Long blogId) {
        List<LikeDto> likes = blogService.getLikesForBlog(blogId);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/{blogId}/comments")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<CommentDto>> getCommentsForBlog(@PathVariable Long blogId) {
        List<CommentDto> comments = blogService.getCommentsForBlog(blogId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/search")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> searchBlogs(
            @RequestParam String query,
            Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.searchBlogs(query, pageable);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/filter")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> filterBlogs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.filterBlogs(category, tags, pageable);
        return ResponseEntity.ok(blogs);
    }
    @DeleteMapping("/soft-delete/{blogId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Void> softDeleteBlog(@PathVariable Long blogId) {
        blogService.softDeleteBlog(blogId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter/advanced")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> advanceFilterBlogs(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.advanceFilterBlogs(authorId, status, category, tags, pageable);
        return ResponseEntity.ok(blogs);
    }

    public ResponseEntity<String> rateLimitFallback(Long blogId, RuntimeConflict e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again later.");
    }
}
