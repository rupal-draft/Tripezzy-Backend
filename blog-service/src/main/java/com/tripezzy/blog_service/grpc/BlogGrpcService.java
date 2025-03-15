package com.tripezzy.blog_service.grpc;

import blog.BlogRequest;
import blog.BlogResponse;
import com.tripezzy.blog_service.dto.BlogResponseDto;
import com.tripezzy.blog_service.service.BlogService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class BlogGrpcService extends blog.BlogServiceGrpc.BlogServiceImplBase {

    private final BlogService blogService;

    public BlogGrpcService(BlogService blogService) {
        this.blogService = blogService;
    }

    @Override
    public void getAllBlogs(BlogRequest request, StreamObserver<BlogResponse> responseObserver) {

        Page<BlogResponseDto> blogPage = blogService
                .getAllBlogs(PageRequest.of(request.getPage(), request.getSize()));

        List<blog.Blog> grpcBlogs = blogPage
                .getContent()
                .stream()
                .map(blogDto ->
                blog.Blog
                        .newBuilder()
                        .setId(blogDto.getId())
                        .setTitle(blogDto.getTitle())
                        .setContent(blogDto.getContent())
                        .setAuthorId(blogDto.getAuthorId())
                        .setCategory(blogDto.getCategory())
                        .setTag(blogDto.getTag())
                        .build()
        ).collect(Collectors.toList());

        BlogResponse response = BlogResponse
                .newBuilder()
                .addAllBlogs(grpcBlogs)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
