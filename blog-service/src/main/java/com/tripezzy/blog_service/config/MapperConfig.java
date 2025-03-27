package com.tripezzy.blog_service.config;

import com.tripezzy.blog_service.dto.BlogResponseDto;
import com.tripezzy.blog_service.entity.Blog;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Blog.class, BlogResponseDto.class).addMappings(mapper -> {
            mapper.map(Blog::getId, BlogResponseDto::setId);
            mapper.map(Blog::getTitle, BlogResponseDto::setTitle);
            mapper.map(Blog::getContent, BlogResponseDto::setContent);
            mapper.map(Blog::getAuthorId, BlogResponseDto::setAuthorId);
            mapper.map(Blog::getCreatedAt, BlogResponseDto::setCreatedAt);
            mapper.map(Blog::getUpdatedAt, BlogResponseDto::setUpdatedAt);
            mapper.map(Blog::getStatus, BlogResponseDto::setStatus);
            mapper.map(Blog::getCategory, BlogResponseDto::setCategory);
            mapper.map(Blog::getTag, BlogResponseDto::setTag);
        });

        return modelMapper;
    }

}
