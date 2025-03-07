package com.tripezzy.blog_service.repository;

import com.tripezzy.blog_service.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBlogId(Long blogId);

    Optional<Comment> findByBlogIdAndId(Long blogId, Long commentId);
}
