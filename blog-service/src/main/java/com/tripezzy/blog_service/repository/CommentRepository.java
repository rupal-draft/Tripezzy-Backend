package com.tripezzy.blog_service.repository;

import com.tripezzy.blog_service.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBlogId(Long blogId);

    Optional<Comment> findByBlogIdAndId(Long blogId, Long commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.content = :content, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :commentId AND c.blog.id = :blogId")
    int updateCommentContent(@Param("blogId") Long blogId, @Param("commentId") Long commentId, @Param("content") String content);
}
