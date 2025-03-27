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
import com.tripezzy.blog_service.exceptions.IllegalState;
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
        UserContext userContext = UserContextHolder.getUserDetails();
        blogDto.setAuthorId(userContext.getUserId());
        Blog blog = modelMapper.map(blogDto, Blog.class);
        blog.setId(null);
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
    @Cacheable(value = "blogsByTags", key = "#tag + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getBlogsByTag(String tag, Pageable pageable) {
        log.info("Fetching blogs by tag: {}", tag);
        return blogRepository.findByTag(tag, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }

    @Override
    @Cacheable(value = "blogsByTags", key = "#tag + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getBlogsByCategory(String category, Pageable pageable) {
        log.info("Fetching blogs by category: {}", category);
        return blogRepository.findByCategory(category, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }

    @Override
    @Cacheable(value = "publishedBlogs", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BlogResponseDto> getPublishedBlogs(Pageable pageable) {
        log.info("Fetching published blogs");
        return blogRepository.findByStatus(BlogStatus.PUBLISHED, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }

    @Override
    @Transactional
    @CacheEvict(value = "blog", key = "#blogId")
    public BlogResponseDto updateBlog(Long blogId, BlogDto blogDto) {
        log.info("Updating blog with ID: {}", blogId);
        UserContext userContext = UserContextHolder.getUserDetails();
        blogDto.setAuthorId(userContext.getUserId());
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        modelMapper.typeMap(BlogDto.class, Blog.class).addMappings(mapper ->
                mapper.skip(Blog::setId)
        );
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
    public void addLikeToBlog(Long blogId) {
        log.info("Adding like to blog with ID: {}", blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        UserContext userContext = UserContextHolder.getUserDetails();
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
        UserContext userContext = UserContextHolder.getUserDetails();
        Comment comment = modelMapper.map(commentDto, Comment.class);
        comment.setBlog(blog);
        comment.setUserId(userContext.getUserId());
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment added successfully with ID: {}", savedComment.getId());
        return modelMapper.map(savedComment, CommentDto.class);
    }

    @Override
    @Transactional
    public void updateComment(Long blogId, Long commentId, String content) {
        log.info("Updating comment content for ID: {} on blog ID: {}", commentId, blogId);

        int updatedRows = commentRepository.updateCommentContent(blogId, commentId, content);

        if (updatedRows == 0) {
            throw new ResourceNotFound("Comment not found with ID: " + commentId);
        }

        log.info("Comment content updated successfully for ID: {}", commentId);
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
    public Integer getLikesCountForBlog(Long blogId) {
        log.info("Fetching likes count for blog with ID: {}", blogId);
        return likeRepository.countByBlogId(blogId);
    }

    @Override
    @Cacheable(value = "commentsForBlog", key = "#blogId")
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
    public BlogResponseDto updateBlogStatus(Long blogId, String status) {
        log.info("Updating blog status with ID: {} to: {}", blogId, status);
        BlogStatus blogStatus = BlogStatus.valueOf(status.toUpperCase());
        Blog blog = blogRepository
                .findById(blogId)
                .orElseThrow(() -> new ResourceNotFound("Blog not found with ID: " + blogId));
        blog.setStatus(blogStatus);
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
            String category,
            String tags,
            Pageable pageable) {
        log.info("Filtering blogs with authorId: {}, category: {}, tags: {}", authorId, category, tags);
        return blogRepository.advanceFilterBlogs(authorId,category, tags, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponseDto.class));
    }
}
