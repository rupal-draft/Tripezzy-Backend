package com.tripezzy.blog_service.controller;

import com.tripezzy.blog_service.advices.ApiError;
import com.tripezzy.blog_service.advices.ApiResponse;
import com.tripezzy.blog_service.annotations.RoleRequired;
import com.tripezzy.blog_service.auth.UserContext;
import com.tripezzy.blog_service.auth.UserContextHolder;
import com.tripezzy.blog_service.dto.BlogDto;
import com.tripezzy.blog_service.dto.BlogResponseDto;
import com.tripezzy.blog_service.dto.CommentDto;
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
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "createBlogRateLimitFallback")
    public ResponseEntity<BlogResponseDto> createBlog(@RequestBody BlogDto blogDto) {
        BlogResponseDto savedBlog = blogService.createBlog(blogDto);
        return new ResponseEntity<>(savedBlog, HttpStatus.CREATED);
    }

    @GetMapping("/public/{blogId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "getBlogByIdRateLimitFallback")
    public ResponseEntity<BlogResponseDto> getBlogById(@PathVariable Long blogId) {
        BlogResponseDto blog = blogService.getBlogById(blogId);
        return ResponseEntity.ok(blog);
    }

    @GetMapping
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "getAllBlogsRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<Page<BlogResponseDto>> getAllBlogs(Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.getAllBlogs(pageable);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/author/{authorId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "getBlogsByAuthorIdRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<Page<BlogResponseDto>> getBlogsByAuthorId(
            @PathVariable Long authorId,
            Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.getBlogsByAuthorId(authorId, pageable);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/public/tags")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "getBlogsByTagsRateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> getBlogsByTags(
            @RequestParam String tags,
            Pageable pageable
    ){
        Page<BlogResponseDto> blogs = blogService.getBlogsByTag(tags, pageable);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/public/category")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "getBlogsByCategoryRateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> getBlogsByCategory(
            @RequestParam String category,
            Pageable pageable
    ){
        Page<BlogResponseDto> blogs = blogService.getBlogsByCategory(category, pageable);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/public/published")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "getPublishedBlogsRateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> getPublishedBlogs(Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.getPublishedBlogs(pageable);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/me")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "getMyBlogsRateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> getMyBlogs(
            Pageable pageable) {
        UserContext userContext = UserContextHolder.getUserDetails();
        Long authorId = userContext.getUserId();
        Page<BlogResponseDto> blogs = blogService.getBlogsByAuthorId(authorId, pageable);
        return ResponseEntity.ok(blogs);
    }

    @PutMapping("/{blogId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "updateBlogRateLimitFallback")
    public ResponseEntity<BlogResponseDto> updateBlog(
            @PathVariable Long blogId,
            @RequestBody BlogDto blogDto) {
        BlogResponseDto updatedBlog = blogService.updateBlog(blogId, blogDto);
        return ResponseEntity.ok(updatedBlog);
    }

    @PutMapping("/{blogId}/status")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "updateBlogStatusRateLimitFallback")
    public ResponseEntity<BlogResponseDto> updateBlogStatus(
            @PathVariable Long blogId,
            @RequestParam String status) {
        BlogResponseDto updatedBlog = blogService.updateBlogStatus(blogId, status);
        return ResponseEntity.ok(updatedBlog);
    }

    @DeleteMapping("/{blogId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "deleteBlogRateLimitFallback")
    public ResponseEntity<ApiResponse<String>> deleteBlog(@PathVariable Long blogId) {
        blogService.deleteBlog(blogId);
        return ResponseEntity.ok(ApiResponse.success("Blog deleted successfully"));
    }

    @PostMapping("/{blogId}/likes")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "addLikeToBlogRateLimitFallback")
    public ResponseEntity<ApiResponse<String>> addLikeToBlog(
            @PathVariable Long blogId) {
        blogService.addLikeToBlog(blogId);
        return new ResponseEntity<>(ApiResponse.success("Like added successfully"), HttpStatus.CREATED);
    }

    @DeleteMapping("/{blogId}/likes/{likeId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "removeLikeFromBlogRateLimitFallback")
    public ResponseEntity<ApiResponse<String>> removeLikeFromBlog(
            @PathVariable Long blogId,
            @PathVariable Long likeId) {
        blogService.removeLikeFromBlog(blogId, likeId);
        return ResponseEntity.ok(ApiResponse.success("Like removed successfully"));
    }

    @PostMapping("/{blogId}/comments")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "addCommentToBlogRateLimitFallback")
    public ResponseEntity<CommentDto> addCommentToBlog(
            @PathVariable Long blogId,
            @RequestBody CommentDto commentDto) {
        CommentDto savedComment = blogService.addCommentToBlog(blogId, commentDto);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    @PutMapping("/{blogId}/comments/{commentId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "updateCommentRateLimitFallback")
    public ResponseEntity<ApiResponse<String>> updateComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @RequestBody CommentDto commentDto) {
        blogService.updateComment(blogId, commentId, commentDto.getContent());
        return ResponseEntity.ok(ApiResponse.success("Comment updated successfully"));
    }

    @DeleteMapping("/{blogId}/comments/{commentId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "deleteCommentRateLimitFallback")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId) {
        blogService.deleteComment(blogId, commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully"));
    }

    @GetMapping("/public/{blogId}/likes")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "getLikesCountForBlogRateLimitFallback")
    public ResponseEntity<ApiResponse<Integer>> getLikesCountForBlog(@PathVariable Long blogId) {
        Integer likes = blogService.getLikesCountForBlog(blogId);
        return ResponseEntity.ok(ApiResponse.success(likes));
    }

    @GetMapping("/public/{blogId}/comments")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "getCommentsForBlogRateLimitFallback")
    public ResponseEntity<List<CommentDto>> getCommentsForBlog(@PathVariable Long blogId) {
        List<CommentDto> comments = blogService.getCommentsForBlog(blogId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/public/search")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "searchBlogsRateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> searchBlogs(
            @RequestParam String query,
            Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.searchBlogs(query, pageable);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/public/filter")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "filterBlogsRateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> filterBlogs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.filterBlogs(category, tags, pageable);
        return ResponseEntity.ok(blogs);
    }

    @DeleteMapping("/soft-delete/{blogId}")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "softDeleteBlogRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<ApiResponse<String>> softDeleteBlog(@PathVariable Long blogId) {
        blogService.softDeleteBlog(blogId);
        return ResponseEntity.ok(ApiResponse.success("Blog soft deleted successfully"));
    }

    @GetMapping("/public/filter/advanced")
    @RateLimiter(name = "blogRateLimiter", fallbackMethod = "advanceFilterBlogsRateLimitFallback")
    public ResponseEntity<Page<BlogResponseDto>> advanceFilterBlogs(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            Pageable pageable) {
        Page<BlogResponseDto> blogs = blogService.advanceFilterBlogs(authorId,category, tags, pageable);
        return ResponseEntity.ok(blogs);
    }

    public ResponseEntity<ApiResponse<String>> rateLimitFallback(String serviceName, Throwable throwable) {
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .setMessage("Too many requests to " + serviceName + ". Please try again later.")
                .setStatus(HttpStatus.TOO_MANY_REQUESTS)
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(apiError));
    }

    public ResponseEntity<ApiResponse<String>> createBlogRateLimitFallback(BlogDto blogDto, Throwable throwable) {
        return rateLimitFallback("createBlog", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBlogByIdRateLimitFallback(Long blogId, Throwable throwable) {
        return rateLimitFallback("getBlogById", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getAllBlogsRateLimitFallback(Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getAllBlogs", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBlogsByAuthorIdRateLimitFallback(Long authorId, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getBlogsByAuthorId", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBlogsByTagsRateLimitFallback(String tags, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getBlogsByTags", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getBlogsByCategoryRateLimitFallback(String category, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getBlogsByCategory", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getPublishedBlogsRateLimitFallback(Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getPublishedBlogs", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getMyBlogsRateLimitFallback(Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getMyBlogs", throwable);
    }

    public ResponseEntity<ApiResponse<String>> updateBlogRateLimitFallback(Long blogId, BlogDto blogDto, Throwable throwable) {
        return rateLimitFallback("updateBlog", throwable);
    }

    public ResponseEntity<ApiResponse<String>> updateBlogStatusRateLimitFallback(Long blogId, String status, Throwable throwable) {
        return rateLimitFallback("updateBlogStatus", throwable);
    }

    public ResponseEntity<ApiResponse<String>> deleteBlogRateLimitFallback(Long blogId, Throwable throwable) {
        return rateLimitFallback("deleteBlog", throwable);
    }

    public ResponseEntity<ApiResponse<String>> addLikeToBlogRateLimitFallback(Long blogId, Throwable throwable) {
        return rateLimitFallback("addLikeToBlog", throwable);
    }

    public ResponseEntity<ApiResponse<String>> removeLikeFromBlogRateLimitFallback(Long blogId, Long likeId, Throwable throwable) {
        return rateLimitFallback("removeLikeFromBlog", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getLikesCountForBlogRateLimitFallback(Long blogId, Throwable throwable) {
        return rateLimitFallback("getLikesCountForBlog", throwable);
    }

    public ResponseEntity<ApiResponse<String>> addCommentToBlogRateLimitFallback(Long blogId, CommentDto commentDto, Throwable throwable) {
        return rateLimitFallback("addCommentToBlog", throwable);
    }

    public ResponseEntity<ApiResponse<String>> updateCommentRateLimitFallback(Long blogId, Long commentId, CommentDto commentDto, Throwable throwable) {
        return rateLimitFallback("updateComment", throwable);
    }

    public ResponseEntity<ApiResponse<String>> deleteCommentRateLimitFallback(Long blogId, Long commentId, Throwable throwable) {
        return rateLimitFallback("deleteComment", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getCommentsForBlogRateLimitFallback(Long blogId, Throwable throwable) {
        return rateLimitFallback("getCommentsForBlog", throwable);
    }

    public ResponseEntity<ApiResponse<String>> searchBlogsRateLimitFallback(String query, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("searchBlogs", throwable);
    }

    public ResponseEntity<ApiResponse<String>> filterBlogsRateLimitFallback(String category, String tags, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("filterBlogs", throwable);
    }

    public ResponseEntity<ApiResponse<String>> advanceFilterBlogsRateLimitFallback(Long authorId, String category, String tags, Pageable pageable, Throwable throwable) {
        return rateLimitFallback("advanceFilterBlogs", throwable);
    }

    public ResponseEntity<ApiResponse<String>> softDeleteBlogRateLimitFallback(Long blogId, Throwable throwable) {
        return rateLimitFallback("softDeleteBlog", throwable);
    }

}
