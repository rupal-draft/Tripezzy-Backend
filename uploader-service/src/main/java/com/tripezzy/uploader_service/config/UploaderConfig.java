package com.tripezzy.uploader_service.config;

import com.cloudinary.Cloudinary;
import com.tripezzy.uploader_service.exceptions.IllegalState;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class UploaderConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @PostConstruct
    public void validateUrl() {
        if (cloudName == null || apiKey == null || apiSecret == null) {
            throw new IllegalState("Cloudinary credentials not found.");
        }
    }

    @Bean
    public Cloudinary cloudinary() {

        Map<String, String> configMap = Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        );

        return new Cloudinary(configMap);
    }
}
