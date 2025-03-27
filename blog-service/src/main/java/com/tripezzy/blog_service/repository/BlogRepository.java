package com.tripezzy.blog_service.repository;

import com.tripezzy.blog_service.entity.Blog;
import com.tripezzy.blog_service.entity.enums.BlogStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    @Query("SELECT b FROM Blog b WHERE b.deleted = false AND b.id = :blogId")
    @Cacheable(value = "blog", key = "#blogId")
    Optional<Blog> findById(@Param("blogId") Long blogId);

    @Query("SELECT b FROM Blog b WHERE b.deleted = false AND b.authorId = :authorId")
    @Cacheable(value = "blogsByAuthor", key = "#authorId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    Page<Blog> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE b.deleted = false AND b.tag = :tag AND b.status = 'PUBLISHED'")
    @Cacheable(value = "blogsByTag", key = "#tag + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    Page<Blog> findByTag(@Param("tag") String tag, Pageable pageable);

    @Cacheable(value = "searchBlogs", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT b FROM Blog b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :query, '%')) AND " +
            "b.deleted = false AND b.status = 'PUBLISHED'")
    Page<Blog> searchBlogs(@Param("query") String query, Pageable pageable);

    @Cacheable(value = "filterBlogs", key = "#category + '-' + #tag + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT b FROM Blog b WHERE " +
            "(:category IS NULL OR b.category = :category) AND " +
            "(:tag IS NULL OR b.tag LIKE CONCAT('%', :tag, '%')) AND " +
            "b.deleted = false AND b.status = 'PUBLISHED'")
    Page<Blog> filterBlogs(
            @Param("category") String category,
            @Param("tag") String tag,
            Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE " +
            "(:authorId IS NULL OR b.authorId = :authorId) AND " +
            "(:category IS NULL OR b.category = :category) AND " +
            "(:tag IS NULL OR b.tag LIKE CONCAT('%', :tag, '%')) AND " +
            "b.deleted = false AND b.status = 'PUBLISHED'")
    @Cacheable(value = "filterBlogs", key = "#authorId + '-' + #status + '-' + #category + '-' + #tag + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    Page<Blog> advanceFilterBlogs(
            @Param("authorId") Long authorId,
            @Param("category") String category,
            @Param("tag") String tag,
            Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE b.deleted = false AND b.category = :category AND b.status = 'PUBLISHED'")
    @Cacheable(value = "blogsByCategory", key = "#category + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    Page<Blog> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE b.deleted = false AND b.status = :status")
    @Cacheable(value = "blogsByStatus", key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    Page<Blog> findByStatus(@Param("status") BlogStatus status, Pageable pageable);
}
