package com.tripezzy.blog_service.repository;

import com.tripezzy.blog_service.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("SELECT COUNT(l) FROM Like l WHERE l.blog.id = :blogId")
    int countByBlogId(@Param("blogId") Long blogId);

    boolean existsByBlogIdAndUser(Long blogId, Long userId);
}
