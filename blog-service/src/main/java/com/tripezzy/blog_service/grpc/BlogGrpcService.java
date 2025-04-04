package com.tripezzy.blog_service.grpc;

import blog.Blog;
import blog.BlogRequest;
import blog.BlogResponse;
import com.tripezzy.blog_service.dto.BlogResponseDto;
import com.tripezzy.blog_service.service.BlogService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class BlogGrpcService extends blog.BlogServiceGrpc.BlogServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BlogGrpcService.class);
    private final BlogService blogService;


    public BlogGrpcService(BlogService blogService) {
        this.blogService = blogService;
    }

    @Override
    public void getAllBlogs(BlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        try {
            log.info("Processing gRPC request for getAllBlogs - page: {}, size: {}", request.getPage(), request.getSize());

            if (request.getPage() < 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Page number cannot be negative")
                        .asRuntimeException();
            }
            if (request.getSize() <= 0 || request.getSize() > 100) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Page size must be between 1 and 100")
                        .asRuntimeException();
            }

            Page<BlogResponseDto> blogPage = blogService.getAllBlogs(
                    PageRequest.of(request.getPage(), request.getSize()));

            List<Blog> grpcBlogs = blogPage.getContent()
                    .stream()
                    .map(this::mapToGrpcBlog)
                    .collect(Collectors.toList());

            BlogResponse response = BlogResponse.newBuilder()
                    .addAllBlogs(grpcBlogs)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully processed getAllBlogs request");

        } catch (StatusRuntimeException e) {
            log.error("gRPC error in getAllBlogs: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in getAllBlogs", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    private Blog mapToGrpcBlog(BlogResponseDto dto) {
        try {
            return Blog.newBuilder()
                    .setId(dto.getId())
                    .setTitle(dto.getTitle())
                    .setContent(dto.getContent())
                    .setAuthorId(dto.getAuthorId())
                    .setCategory(dto.getCategory())
                    .setTag(dto.getTag())
                    .build();
        } catch (Exception e) {
            log.error("Mapping error for blog ID: {}", dto.getId(), e);
            throw Status.INTERNAL
                    .withDescription("Failed to process blog data")
                    .asRuntimeException();
        }
    }
}
