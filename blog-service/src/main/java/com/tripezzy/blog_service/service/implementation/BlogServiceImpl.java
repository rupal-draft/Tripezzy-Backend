package com.tripezzy.blog_service.service.implementation;

import com.tripezzy.blog_service.auth.UserContext;
import com.tripezzy.blog_service.auth.UserContextHolder;
import com.tripezzy.blog_service.dto.BlogDto;
import com.tripezzy.blog_service.dto.BlogResponseDto;
import com.tripezzy.blog_service.dto.CommentDto;
import com.tripezzy.blog_service.entity.Blog;
import com.tripezzy.blog_service.entity.Comment;
import com.tripezzy.blog_service.entity.Like;
import com.tripezzy.blog_service.entity.enums.BlogStatus;
import com.tripezzy.blog_service.events.BlogCommentedEvent;
import com.tripezzy.blog_service.events.BlogCreatedEvent;
import com.tripezzy.blog_service.events.BlogLikedEvent;
import com.tripezzy.blog_service.exceptions.*;
import com.tripezzy.blog_service.repository.BlogRepository;
import com.tripezzy.blog_service.repository.CommentRepository;
import com.tripezzy.blog_service.repository.LikeRepository;
import com.tripezzy.blog_service.service.BlogService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);
    private final BlogRepository blogRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;

    private final KafkaTemplate<Long, BlogCreatedEvent> kafkaBlogCreatedTemplate;
    private final KafkaTemplate<Long, BlogLikedEvent> kafkaBlogLikedTemplate;
    private final KafkaTemplate<Long, BlogCommentedEvent> kafkaBlogCommentedTemplate;

    public BlogServiceImpl(BlogRepository blogRepository,
                           LikeRepository likeRepository,
                           CommentRepository commentRepository,
                           ModelMapper modelMapper,
                           KafkaTemplate<Long, BlogCreatedEvent> kafkaBlogCreatedTemplate,
                           KafkaTemplate<Long, BlogLikedEvent> kafkaBlogLikedTemplate,
                           KafkaTemplate<Long, BlogCommentedEvent> kafkaBlogCommentedTemplate) {
        this.blogRepository = blogRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.modelMapper = modelMapper;
        this.kafkaBlogCreatedTemplate = kafkaBlogCreatedTemplate;
        this.kafkaBlogLikedTemplate = kafkaBlogLikedTemplate;
        this.kafkaBlogCommentedTemplate = kafkaBlogCommentedTemplate;
    }

    @Override
    @Transactional
    public BlogResponseDto createBlog(BlogDto blogDto) {
        try {
            log.info("Creating a new blog: {}", blogDto.getTitle());

            if (blogDto.getTitle() == null || blogDto.getTitle().isBlank()) {
                throw new BadRequestException("Blog title is required");
            }
            if (blogDto.getContent() == null || blogDto.getContent().isBlank()) {
                throw new BadRequestException("Blog content is required");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            blogDto.setAuthorId(userContext.getUserId());
            Blog blog = modelMapper.map(blogDto, Blog.class);
            blog.setId(null);

            Blog savedBlog = blogRepository.save(blog);
            log.info("Blog created successfully with ID: {}", savedBlog.getId());

            log.info("Sending blog created event");
            BlogCreatedEvent blogCreatedEvent = new BlogCreatedEvent(savedBlog.getId(), savedBlog.getTitle(), savedBlog.getAuthorId());
            kafkaBlogCreatedTemplate.send("new-blog", savedBlog.getId(), blogCreatedEvent);
            log.info("Blog created event sent");

            return modelMapper.map(savedBlog, BlogResponseDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while creating blog", ex);
            throw new DataIntegrityViolation("Failed to create blog due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error while creating blog", ex);
            throw new IllegalState("Failed to map blog data");
        } catch (KafkaException ex) {
            log.error("Kafka error while sending blog created event", ex);
            throw new KafkaException("Failed to send blog created event");
        }
    }

    @Override
    @Cacheable(value = "blog", key = "#blogId")
    public BlogResponseDto getBlogById(Long blogId) {
        try {
            log.info("Fetching blog by ID: {}", blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }

            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));

            return modelMapper.map(blog, BlogResponseDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching blog ID {}", blogId, ex);
            throw new ServiceUnavailable("Unable to retrieve blog at this time");
        }
    }

    @Override
    @Cacheable(value = "blogs", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getAllBlogs(Pageable pageable) {
        try {
            log.info("Fetching all blogs (paginated)");

            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return blogRepository.findAll(pageable)
                    .map(blog -> {
                        try {
                            return modelMapper.map(blog, BlogResponseDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for blog ID {}", blog.getId(), ex);
                            throw new IllegalState("Failed to map blog data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching all blogs", ex);
            throw new ServiceUnavailable("Unable to retrieve blogs at this time");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "blog", key = "#blogId")
    public BlogResponseDto updateBlog(Long blogId, BlogDto blogDto) {
        try {
            log.info("Updating blog with ID: {}", blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }
            if (blogDto == null) {
                throw new BadRequestException("Blog data cannot be null");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));

            
            if (!blog.getAuthorId().equals(userContext.getUserId())) {
                throw new AccessForbidden("You are not authorized to update this blog");
            }

            modelMapper.typeMap(BlogDto.class, Blog.class).addMappings(mapper ->
                    mapper.skip(Blog::setId)
            );
            modelMapper.map(blogDto, blog);

            Blog updatedBlog = blogRepository.save(blog);
            log.info("Blog updated successfully with ID: {}", blogId);

            return modelMapper.map(updatedBlog, BlogResponseDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while updating blog ID {}", blogId, ex);
            throw new DataIntegrityViolation("Failed to update blog due to database error");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "blog", key = "#blogId")
    public void deleteBlog(Long blogId) {
        try {
            log.info("Deleting blog with ID: {}", blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));

            
            if (!blog.getAuthorId().equals(userContext.getUserId())) {
                throw new AccessForbidden("You are not authorized to delete this blog");
            }

            blogRepository.delete(blog);
            log.info("Blog deleted successfully with ID: {}", blogId);

        } catch (DataAccessException ex) {
            log.error("Database error while deleting blog ID {}", blogId, ex);
            throw new DataIntegrityViolation("Failed to delete blog due to database error");
        }
    }

    @Override
    @Transactional
    public void addLikeToBlog(Long blogId) {
        try {
            log.info("Adding like to blog with ID: {}", blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));

            Long userId = userContext.getUserId();
            boolean alreadyLiked = likeRepository.existsByBlogIdAndUser(blogId, userId);
            if (alreadyLiked) {
                throw new IllegalState("User has already liked this blog");
            }

            Like like = new Like();
            like.setBlog(blog);
            like.setUserId(userId);

            Like savedLike = likeRepository.save(like);
            log.info("Like added successfully with ID: {}", savedLike.getId());

            log.info("Sending blog liked event");
            BlogLikedEvent blogLikedEvent = new BlogLikedEvent(blogId, userId);
            kafkaBlogLikedTemplate.send("blog-liked", blogId, blogLikedEvent);
            log.info("Blog liked event sent");

        } catch (DataAccessException ex) {
            log.error("Database error while adding like to blog ID {}", blogId, ex);
            throw new DataIntegrityViolation("Failed to add like due to database error");
        } catch (KafkaException ex) {
            log.error("Kafka error while adding like to blog ID {}", blogId, ex);
            throw new KafkaException("Failed to add like due to Kafka error");
        }
    }

    @Override
    @Cacheable(value = "commentsForBlog", key = "#blogId")
    public List<CommentDto> getCommentsForBlog(Long blogId) {
        try {
            log.info("Fetching comments for blog with ID: {}", blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }

            List<Comment> comments = commentRepository.findByBlogId(blogId);
            return comments.stream()
                    .map(comment -> {
                        try {
                            return modelMapper.map(comment, CommentDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for comment ID {}", comment.getId(), ex);
                            throw new IllegalState("Failed to map comment data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while fetching comments for blog ID {}", blogId, ex);
            throw new ServiceUnavailable("Unable to retrieve comments at this time");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "blog", key = "#blogId")
    public BlogResponseDto updateBlogStatus(Long blogId, String status) {
        try {
            log.info("Updating blog status with ID: {} to: {}", blogId, status);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }
            if (status == null || status.isBlank()) {
                throw new BadRequestException("Status cannot be empty");
            }

            BlogStatus blogStatus;
            try {
                blogStatus = BlogStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid blog status: " + status);
            }

            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));

            blog.setStatus(blogStatus);
            Blog updatedBlog = blogRepository.save(blog);
            log.info("Blog status updated successfully with ID: {}", blogId);

            return modelMapper.map(updatedBlog, BlogResponseDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while updating status for blog ID {}", blogId, ex);
            throw new DataIntegrityViolation("Failed to update blog status due to database error");
        }
    }

    @Override
    @Cacheable(value = "blogsByAuthor", key = "#authorId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getBlogsByAuthorId(Long authorId, Pageable pageable) {
        try {
            log.info("Fetching blogs by author ID: {}", authorId);

            if (authorId == null || authorId <= 0) {
                throw new BadRequestException("Invalid author ID");
            }
            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return blogRepository.findByAuthorId(authorId, pageable)
                    .map(blog -> {
                        try {
                            return modelMapper.map(blog, BlogResponseDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for blog ID {}", blog.getId(), ex);
                            throw new IllegalState("Failed to map blog data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching blogs by author ID {}", authorId, ex);
            throw new ServiceUnavailable("Unable to retrieve blogs at this time");
        }
    }

    @Override
    @Cacheable(value = "blogsByTags", key = "#tag + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getBlogsByTag(String tag, Pageable pageable) {
        try {
            log.info("Fetching blogs by tag: {}", tag);

            if (tag == null || tag.isBlank()) {
                throw new BadRequestException("Tag cannot be empty");
            }
            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return blogRepository.findByTag(tag, pageable)
                    .map(blog -> {
                        try {
                            return modelMapper.map(blog, BlogResponseDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for blog ID {}", blog.getId(), ex);
                            throw new IllegalState("Failed to map blog data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching blogs by tag {}", tag, ex);
            throw new ServiceUnavailable("Unable to retrieve blogs at this time");
        }
    }

    @Override
    @Cacheable(value = "blogsByCategory", key = "#category + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getBlogsByCategory(String category, Pageable pageable) {
        try {
            log.info("Fetching blogs by category: {}", category);

            if (category == null || category.isBlank()) {
                throw new BadRequestException("Category cannot be empty");
            }
            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return blogRepository.findByCategory(category, pageable)
                    .map(blog -> {
                        try {
                            return modelMapper.map(blog, BlogResponseDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for blog ID {}", blog.getId(), ex);
                            throw new IllegalState("Failed to map blog data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching blogs by category {}", category, ex);
            throw new ServiceUnavailable("Unable to retrieve blogs at this time");
        }
    }

    @Override
    @Cacheable(value = "publishedBlogs", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getPublishedBlogs(Pageable pageable) {
        try {
            log.info("Fetching published blogs");

            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return blogRepository.findByStatus(BlogStatus.PUBLISHED, pageable)
                    .map(blog -> {
                        try {
                            return modelMapper.map(blog, BlogResponseDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for blog ID {}", blog.getId(), ex);
                            throw new IllegalState("Failed to map blog data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching published blogs", ex);
            throw new ServiceUnavailable("Unable to retrieve blogs at this time");
        }
    }

    @Override
    @Transactional
    public void removeLikeFromBlog(Long blogId, Long likeId) {
        try {
            log.info("Removing like with ID: {} from blog with ID: {}", likeId, blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }
            if (likeId == null || likeId <= 0) {
                throw new BadRequestException("Invalid like ID");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Like like = likeRepository.findById(likeId)
                    .orElseThrow(() -> new ResourceNotFound("Like not found with ID: " + likeId));

            
            if (!like.getUserId().equals(userContext.getUserId())) {
                throw new AccessForbidden("You are not authorized to remove this like");
            }

            likeRepository.delete(like);
            log.info("Like removed successfully with ID: {}", likeId);

        } catch (DataAccessException ex) {
            log.error("Database error while removing like ID {}", likeId, ex);
            throw new DataIntegrityViolation("Failed to remove like due to database error");
        }
    }

    @Override
    @Transactional
    public CommentDto addCommentToBlog(Long blogId, CommentDto commentDto) {
        try {
            log.info("Adding comment to blog with ID: {}", blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }
            if (commentDto == null || commentDto.getContent() == null || commentDto.getContent().isBlank()) {
                throw new BadRequestException("Comment content is required");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));

            Comment comment = modelMapper.map(commentDto, Comment.class);
            comment.setBlog(blog);
            comment.setUserId(userContext.getUserId());

            Comment savedComment = commentRepository.save(comment);
            log.info("Comment added successfully with ID: {}", savedComment.getId());

            log.info("Sending blog commented event");
            BlogCommentedEvent blogCommentedEvent = new BlogCommentedEvent(blogId, userContext.getUserId(), savedComment.getId());
            kafkaBlogCommentedTemplate.send("blog-commented", savedComment.getId() ,blogCommentedEvent);
            log.info("Blog commented event sent");

            return modelMapper.map(savedComment, CommentDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while adding comment to blog ID {}", blogId, ex);
            throw new DataIntegrityViolation("Failed to add comment due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error while adding comment", ex);
            throw new IllegalState("Failed to map comment data");
        } catch (KafkaException ex) {
            log.error("Kafka error while sending blog commented event", ex);
            throw new KafkaException("Failed to send blog commented event");
        }
    }

    @Override
    @Transactional
    public void updateComment(Long blogId, Long commentId, String content) {
        try {
            log.info("Updating comment content for ID: {} on blog ID: {}", commentId, blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }
            if (commentId == null || commentId <= 0) {
                throw new BadRequestException("Invalid comment ID");
            }
            if (content == null || content.isBlank()) {
                throw new BadRequestException("Comment content cannot be empty");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Comment comment = commentRepository.findByBlogIdAndId(blogId, commentId)
                    .orElseThrow(() -> new ResourceNotFound("Comment not found with ID: " + commentId));

            
            if (!comment.getUserId().equals(userContext.getUserId())) {
                throw new AccessForbidden("You are not authorized to update this comment");
            }

            comment.setContent(content);
            commentRepository.save(comment);
            log.info("Comment content updated successfully for ID: {}", commentId);

        } catch (DataAccessException ex) {
            log.error("Database error while updating comment ID {}", commentId, ex);
            throw new DataIntegrityViolation("Failed to update comment due to database error");
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long blogId, Long commentId) {
        try {
            log.info("Deleting comment with ID: {} from blog with ID: {}", commentId, blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }
            if (commentId == null || commentId <= 0) {
                throw new BadRequestException("Invalid comment ID");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Comment comment = commentRepository.findByBlogIdAndId(blogId, commentId)
                    .orElseThrow(() -> new ResourceNotFound("Comment not found with ID: " + commentId));

            
            if (!comment.getUserId().equals(userContext.getUserId())) {
                throw new AccessForbidden("You are not authorized to delete this comment");
            }

            commentRepository.delete(comment);
            log.info("Comment deleted successfully with ID: {}", commentId);

        } catch (DataAccessException ex) {
            log.error("Database error while deleting comment ID {}", commentId, ex);
            throw new DataIntegrityViolation("Failed to delete comment due to database error");
        }
    }

    @Override
    @Cacheable(value = "likesForBlog", key = "#blogId")
    public Integer getLikesCountForBlog(Long blogId) {
        try {
            log.info("Fetching likes count for blog with ID: {}", blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }

            return likeRepository.countByBlogId(blogId);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching likes count for blog ID {}", blogId, ex);
            throw new ServiceUnavailable("Unable to retrieve likes count at this time");
        }
    }

    @Override
    @Cacheable(value = "searchBlogs", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> searchBlogs(String query, Pageable pageable) {
        try {
            log.info("Searching blogs with query: {}", query);

            if (query == null || query.isBlank()) {
                throw new BadRequestException("Search query cannot be empty");
            }
            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return blogRepository.searchBlogs(query, pageable)
                    .map(blog -> {
                        try {
                            return modelMapper.map(blog, BlogResponseDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for blog ID {}", blog.getId(), ex);
                            throw new IllegalState("Failed to map blog data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while searching blogs with query {}", query, ex);
            throw new ServiceUnavailable("Unable to search blogs at this time");
        }
    }

    @Override
    @Cacheable(value = "filterBlogs", key = "#category + '-' + #tags + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> filterBlogs(String category, String tags, Pageable pageable) {
        try {
            log.info("Filtering blogs with category: {} and tags: {}", category, tags);

            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return blogRepository.filterBlogs(category, tags, pageable)
                    .map(blog -> {
                        try {
                            return modelMapper.map(blog, BlogResponseDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for blog ID {}", blog.getId(), ex);
                            throw new IllegalState("Failed to map blog data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while filtering blogs", ex);
            throw new ServiceUnavailable("Unable to filter blogs at this time");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "blog", key = "#blogId")
    public void softDeleteBlog(Long blogId) {
        try {
            log.info("Soft deleting blog with ID: {}", blogId);

            if (blogId == null || blogId <= 0) {
                throw new BadRequestException("Invalid blog ID");
            }

            UserContext userContext = UserContextHolder.getUserDetails();
            if (userContext == null || userContext.getUserId() == null) {
                throw new AccessForbidden("Authentication required");
            }

            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));

            
            if (!blog.getAuthorId().equals(userContext.getUserId())) {
                throw new AccessForbidden("You are not authorized to delete this blog");
            }

            blog.setDeleted(true);
            blogRepository.save(blog);
            log.info("Blog soft deleted successfully with ID: {}", blogId);

        } catch (DataAccessException ex) {
            log.error("Database error while soft deleting blog ID {}", blogId, ex);
            throw new DataIntegrityViolation("Failed to soft delete blog due to database error");
        }
    }

    @Override
    @Cacheable(value = "advanceFilterBlogs",
            key = "#authorId + '-' + #category + '-' + #tags + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> advanceFilterBlogs(Long authorId, String category, String tags, Pageable pageable) {
        try {
            log.info("Filtering blogs with authorId: {}, category: {}, tags: {}", authorId, category, tags);

            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }

            return blogRepository.advanceFilterBlogs(authorId, category, tags, pageable)
                    .map(blog -> {
                        try {
                            return modelMapper.map(blog, BlogResponseDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for blog ID {}", blog.getId(), ex);
                            throw new IllegalState("Failed to map blog data");
                        }
                    });

        } catch (DataAccessException ex) {
            log.error("Database error while advance filtering blogs", ex);
            throw new ServiceUnavailable("Unable to filter blogs at this time");
        }
    }
}
