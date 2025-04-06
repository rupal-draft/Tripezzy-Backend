package com.tripezzy.blog_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic newBlogTopic(){
        return new NewTopic("new-blog", 3, (short) 1);
    }

    @Bean
    public NewTopic blogLikedTopic(){
        return new NewTopic("blog-liked", 3, (short) 1);
    }

    @Bean
    public NewTopic blogCommentedTopic(){
        return new NewTopic("blog-commented", 3, (short) 1);
    }
}
