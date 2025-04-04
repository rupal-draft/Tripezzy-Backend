package com.tripezzy.admin_service.grpc;

import blog.Blog;
import blog.BlogRequest;
import blog.BlogResponse;
import blog.BlogServiceGrpc;
import com.tripezzy.admin_service.dto.BlogResponseDto;
import com.tripezzy.admin_service.exceptions.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BlogGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BlogGrpcClient.class);
    private final BlogServiceGrpc.BlogServiceBlockingStub blogStub;
    private final ManagedChannel channel;

    public BlogGrpcClient() {
        try {
            this.channel = ManagedChannelBuilder
                    .forAddress("localhost", 9001)
                    .usePlaintext()
                    .build();

            this.blogStub = BlogServiceGrpc.newBlockingStub(channel);
            checkServiceHealth();
        } catch (Exception e) {
            log.error("Failed to initialize gRPC blog client", e);
            throw new ServiceUnavailable("Blog service is currently unavailable");
        }
    }

    private void checkServiceHealth() {
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(
                    HealthCheckRequest.newBuilder().build());

            if (response.getStatus() != HealthCheckResponse.ServingStatus.SERVING) {
                log.error("Blog service is not healthy: {}", response.getStatus());
                throw new ServiceUnavailable("Blog service is not healthy");
            }
            log.info("Blog service health status: {}", response.getStatus());
        } catch (StatusRuntimeException e) {
            log.error("Blog service health check failed", e);
            throw new ServiceUnavailable("Blog service is unreachable");
        }
    }

    @Cacheable(value = "blogs", key = "#page + '-' + #size")
    public List<BlogResponseDto> getAllBlogs(int page, int size) {
        validatePaginationParams(page, size);
        log.info("Fetching all blogs - page: {}, size: {}", page, size);

        try {
            BlogRequest request = BlogRequest.newBuilder()
                    .setPage(page)
                    .setSize(size)
                    .build();

            BlogResponse response = blogStub.getAllBlogs(request);

            if (response == null || response.getBlogsList().isEmpty()) {
                log.warn("No blogs found for page {} and size {}", page, size);
                return Collections.emptyList();
            }

            return mapBlogsResponse(response);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get blogs");
            return Collections.emptyList();
        }
    }

    private List<BlogResponseDto> mapBlogsResponse(BlogResponse response) {
        return response.getBlogsList().stream()
                .map(this::mapBlog)
                .collect(Collectors.toUnmodifiableList());
    }

    private BlogResponseDto mapBlog(Blog blog) {
        try {
            return new BlogResponseDto(
                    blog.getId(),
                    blog.getTitle(),
                    blog.getContent(),
                    blog.getAuthorId(),
                    blog.getCategory(),
                    blog.getTag()
            );
        } catch (Exception e) {
            log.error("Failed to map blog data for ID: {}", blog.getId(), e);
            throw new IllegalState("Failed to process blog data");
        }
    }

    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }

    private void handleGrpcException(StatusRuntimeException e, String context) {
        Status.Code code = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        log.error("gRPC error [{}] {}: {}", code, context, description, e);

        switch (code) {
            case NOT_FOUND:
                throw new ResourceNotFound(description != null ? description : "Requested blog not found");
            case INVALID_ARGUMENT:
                throw new BadRequestException(description != null ? description : "Invalid blog request parameters");
            case PERMISSION_DENIED:
                throw new AccessForbidden(description != null ? description : "Blog permission denied");
            case UNAVAILABLE:
                throw new ServiceUnavailable("Blog service is currently unavailable");
            case FAILED_PRECONDITION:
                throw new IllegalState(description != null ? description : "Invalid blog state");
            default:
                throw new ServiceUnavailable("Failed to process blog request");
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            log.warn("Failed to shutdown gRPC channel properly", e);
            Thread.currentThread().interrupt();
        }
    }
}
