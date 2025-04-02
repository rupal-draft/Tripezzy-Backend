package com.tripezzy.admin_service.grpc;

import blog.BlogRequest;
import blog.BlogResponse;
import blog.BlogServiceGrpc;
import com.tripezzy.admin_service.dto.BlogResponseDto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BlogGrpcClient.class);
    private final BlogServiceGrpc.BlogServiceBlockingStub stub;


    public BlogGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9001)
                .usePlaintext()
                .build();
        stub = BlogServiceGrpc.newBlockingStub(channel);
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());
            log.info("Health Status: " + response.getStatus());
        } catch (Exception e) {
            log.error("Blog service is unreachable. Proceeding without it.");
        }
    }

    @Cacheable(value = "blogs", key = "#page + '-' + #size")
    public List<BlogResponseDto> getAllBlogs(int page, int size) {
        log.info("Getting all blogs");
        BlogRequest request = BlogRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();

        BlogResponse response = stub.getAllBlogs(request);
        return response.getBlogsList().stream().map(blog ->
                new BlogResponseDto(
                        blog.getId(),
                        blog.getTitle(),
                        blog.getContent(),
                        blog.getAuthorId(),
                        blog.getCategory(),
                        blog.getTag()
                )
        ).collect(Collectors.toList());
    }
}
