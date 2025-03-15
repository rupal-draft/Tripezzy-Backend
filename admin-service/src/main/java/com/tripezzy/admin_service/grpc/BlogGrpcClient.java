package com.tripezzy.admin_service.grpc;

import blog.Blog;
import blog.BlogRequest;
import blog.BlogResponse;
import blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogGrpcClient {

    private final BlogServiceGrpc.BlogServiceBlockingStub stub;

    public BlogGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("blog-service", 9001)
                .usePlaintext()
                .build();
        stub = BlogServiceGrpc.newBlockingStub(channel);
    }

    @Cacheable(value = "blogs", key = "#page + '-' + #size")
    public List<Blog> getAllBlogs(int page, int size) {
        BlogRequest request = BlogRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();

        BlogResponse response = stub.getAllBlogs(request);
        return response.getBlogsList();
    }
}
