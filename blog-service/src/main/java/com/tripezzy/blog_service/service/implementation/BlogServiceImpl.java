package com.tripezzy.blog_service.service.implementation;

import com.tripezzy.blog_service.dto.BlogDto;
import com.tripezzy.blog_service.dto.BlogResponseDto;
import com.tripezzy.blog_service.dto.CommentDto;
import com.tripezzy.blog_service.dto.LikeDto;
import com.tripezzy.blog_service.entity.Blog;
import com.tripezzy.blog_service.entity.Comment;
import com.tripezzy.blog_service.entity.Like;
import com.tripezzy.blog_service.entity.enums.BlogStatus;
import com.tripezzy.blog_service.exceptions.ResourceNotFound;
import com.tripezzy.blog_service.repository.BlogRepository;
import com.tripezzy.blog_service.repository.CommentRepository;
import com.tripezzy.blog_service.repository.LikeRepository;
import com.tripezzy.blog_service.service.BlogService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {
    private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);

    private final BlogRepository blogRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;

    public BlogServiceImpl(
            BlogRepository blogRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            ModelMapper modelMapper) {
        this.blogRepository = blogRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public BlogResponseDto createBlog(BlogDto blogDto) {
        log.info("Creating a new blog: {}", blogDto.getTitle());
        Blog blog = modelMapper.map(blogDto, Blog.class);
        Blog savedBlog = blogRepository.save(blog);
        log.info("Blog created successfully with ID: {}", savedBlog.getId());
        return modelMapper.map(savedBlog, BlogResponseDto.class);
    }

    @Override
    @Cacheable(value = "blog", key = "#blogId")
    public BlogResponseDto getBlogById(Long blogId) {
        log.info("Fetching blog by ID: {}", blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        return modelMapper.map(blog, BlogResponseDto.class);
    }

    @Override
    @Cacheable(value = "blogs", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getAllBlogs(Pageable pageable) {
        log.info("Fetching all blogs (paginated)");
        return blogRepository.findAll(pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }

    @Override
    @Cacheable(value = "blogsByAuthor", key = "#authorId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getBlogsByAuthorId(Long authorId, Pageable pageable) {
        log.info("Fetching blogs by author ID: {}", authorId);
        return blogRepository.findByAuthorId(authorId, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }

    @Override
    @Cacheable(value = "blogsByStatus", key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getBlogsByStatus(String status, Pageable pageable) {
        log.info("Fetching blogs by status: {}", status);
        BlogStatus blogStatus = BlogStatus.valueOf(status.toUpperCase());
        return blogRepository.findByStatus(blogStatus, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }

    @Override
    @Transactional
    @CacheEvict(value = "blog", key = "#blogId")
    public BlogResponseDto updateBlog(Long blogId, BlogDto blogDto) {
        log.info("Updating blog with ID: {}", blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        modelMapper.map(blogDto, blog);
        Blog updatedBlog = blogRepository.save(blog);
        log.info("Blog updated successfully with ID: {}", blogId);
        return modelMapper.map(updatedBlog, BlogResponseDto.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "blog", key = "#blogId")
    public void deleteBlog(Long blogId) {
        log.info("Deleting blog with ID: {}", blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        blogRepository.delete(blog);
        log.info("Blog deleted successfully with ID: {}", blogId);
    }

    @Override
    @Transactional
    public LikeDto addLikeToBlog(Long blogId, LikeDto likeDto) {
        log.info("Adding like to blog with ID: {}", blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        Like like = modelMapper.map(likeDto, Like.class);
        like.setBlog(blog);
        Like savedLike = likeRepository.save(like);
        log.info("Like added successfully with ID: {}", savedLike.getId());
        return modelMapper.map(savedLike, LikeDto.class);
    }

    @Override
    @Transactional
    public void removeLikeFromBlog(Long blogId, Long likeId) {
        log.info("Removing like with ID: {} from blog with ID: {}", likeId, blogId);
        Like like = likeRepository.findById(likeId)
                .orElseThrow(() -> new ResourceNotFound("Like not found with ID: " + likeId));
        likeRepository.delete(like);
        log.info("Like removed successfully with ID: {}", likeId);
    }

    @Override
    @Transactional
    public CommentDto addCommentToBlog(Long blogId, CommentDto commentDto) {
        log.info("Adding comment to blog with ID: {}", blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        Comment comment = modelMapper.map(commentDto, Comment.class);
        comment.setBlog(blog);
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment added successfully with ID: {}", savedComment.getId());
        return modelMapper.map(savedComment, CommentDto.class);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long blogId, Long commentId, CommentDto commentDto) {
        log.info("Updating comment with ID: {} on blog with ID: {}", commentId, blogId);
        Comment comment = commentRepository.findByBlogIdAndId(blogId, commentId)
                .orElseThrow(() -> new ResourceNotFound("Comment not found with ID: " + commentId));
        modelMapper.map(commentDto, comment);
        comment.setUpdatedAt(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment updated successfully with ID: {}", commentId);
        return modelMapper.map(updatedComment, CommentDto.class);
    }

    @Override
    @Transactional
    public void deleteComment(Long blogId, Long commentId) {
        log.info("Deleting comment with ID: {} from blog with ID: {}", commentId, blogId);
        Comment comment = commentRepository.findByBlogIdAndId(blogId, commentId)
                .orElseThrow(() -> new ResourceNotFound("Comment not found with ID: " + commentId));
        commentRepository.delete(comment);
        log.info("Comment deleted successfully with ID: {}", commentId);
    }

    @Override
    @Cacheable(value = "likesForBlog", key = "#blogId")
    public List<LikeDto> getLikesForBlog(Long blogId) {
        log.info("Fetching likes for blog with ID: {}", blogId);
        return likeRepository.findByBlogId(blogId).stream()
                .map(like -> modelMapper.map(like, LikeDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "commentsForBlog", key = "#blogId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public List<CommentDto> getCommentsForBlog(Long blogId) {
        log.info("Fetching comments for blog with ID: {}", blogId);
        List<Comment> comments = commentRepository.findByBlogId(blogId);
        return comments
                .stream()
                .map(comment -> modelMapper
                        .map(comment, CommentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "searchBlogs", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> searchBlogs(String query, Pageable pageable) {
        log.info("Searching blogs with query: {}", query);
        return blogRepository.searchBlogs(query, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }

    @Override
    @Cacheable(value = "filterBlogs", key = "#category + '-' + #tags + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> filterBlogs(String category, String tags, Pageable pageable) {
        log.info("Filtering blogs with category: {} and tags: {}", category, tags);
        return blogRepository.filterBlogs(category, tags, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }

    @Override
    @Transactional
    @CacheEvict(value = "blog", key = "#blogId")
    public BlogResponseDto updateBlogStatus(Long blogId, BlogStatus status) {
        log.info("Updating blog status with ID: {} to: {}", blogId, status);
        Blog blog = blogRepository
                .findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        blog.setStatus(status);
        Blog updatedBlog = blogRepository.save(blog);
        log.info("Blog status updated successfully with ID: {}", blogId);
        return modelMapper.map(updatedBlog, BlogResponseDto.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "blog", key = "#blogId")
    public void softDeleteBlog(Long blogId) {
        log.info("Soft deleting blog with ID: {}", blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        blog.setDeleted(true);
        blogRepository.save(blog);
        log.info("Blog soft deleted successfully with ID: {}", blogId);
    }

    @Override
    @Cacheable(value = "filterBlogs", key = "#authorId + '-' + #status + '-' + #category + '-' + #tags + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> advanceFilterBlogs(
            Long authorId,
            BlogStatus status,
            String category,
            String tags,
            Pageable pageable) {
        log.info("Filtering blogs with authorId: {}, status: {}, category: {}, tags: {}", authorId, status, category, tags);
        return blogRepository.advanceFilterBlogs(authorId, status, category, tags, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }
}
