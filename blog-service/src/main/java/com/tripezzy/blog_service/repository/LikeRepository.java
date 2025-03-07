package com.tripezzy.blog_service.repository;

import com.tripezzy.blog_service.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    List<Like> findByBlogId(Long blogId);
}
