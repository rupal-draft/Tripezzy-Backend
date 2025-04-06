package com.tripezzy.notification_service.grpc;

import com.google.protobuf.Empty;
import com.tripezzy.grpc.user.UserIdRequest;
import com.tripezzy.grpc.user.UserListResponse;
import com.tripezzy.grpc.user.UserServiceGrpc;
import com.tripezzy.notification_service.dto.UserDto;
import com.tripezzy.notification_service.exceptions.ResourceNotFound;
import com.tripezzy.notification_service.exceptions.ServiceUnavailable;
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
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class UserGrpcClient {


    private static final Logger log = LoggerFactory.getLogger(UserGrpcClient.class);
    private final UserServiceGrpc.UserServiceBlockingStub userStub;
    private final ManagedChannel channel;

    public UserGrpcClient() {
        this.channel = ManagedChannelBuilder
                .forAddress("localhost", 6001)
                .usePlaintext()
                .build();
        this.userStub = UserServiceGrpc.newBlockingStub(channel);
        checkServiceHealth();
    }

    private void checkServiceHealth() {
        HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
        try {
            HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());
            if (response.getStatus() != HealthCheckResponse.ServingStatus.SERVING) {
                log.error("User service is not healthy: {}", response.getStatus());
                throw new ServiceUnavailable("User service is not healthy");
            }
            log.info("User service health status: {}", response.getStatus());
        } catch (StatusRuntimeException e) {
            log.error("User service health check failed", e);
            throw new ServiceUnavailable("User service is unreachable");
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

    private void handleGrpcException(StatusRuntimeException e, String context) {
        Status.Code code = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        log.error("gRPC error [{}] {}: {}", code, context, description, e);

        switch (code) {
            case NOT_FOUND:
                throw new ResourceNotFound(description != null ? description : "Booking not found");
            case INVALID_ARGUMENT:
                throw new IllegalArgumentException(description != null ? description : "Invalid request parameters");
            case UNAVAILABLE:
                throw new ServiceUnavailable("Booking service is currently unavailable");
            default:
                throw new ServiceUnavailable("Failed to process booking request");
        }
    }

    private List<UserDto> getUserList(Supplier<UserListResponse> grpcCall, String context) {
        log.info("Getting user list for {}", context);
        try {
            UserListResponse response = grpcCall.get();
            if (response == null || response.getUsersList().isEmpty()) {
                log.warn("No users found for {}", context);
                return Collections.emptyList();
            }
            return mapUserResponse(response);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get users for " + context);
            return Collections.emptyList();
        }
    }

    public List<UserDto> getAllAdminUsers() {
        return getUserList(() -> userStub.getAllAdminUsers(Empty.newBuilder().build()), "admin users");
    }

    public List<UserDto> getAllSellerUsers() {
        return getUserList(() -> userStub.getAllSellerUsers(Empty.newBuilder().build()), "seller users");
    }

    public List<UserDto> getAllGuideUsers() {
        return getUserList(() -> userStub.getAllGuideUsers(Empty.newBuilder().build()), "guide users");
    }

    public List<UserDto> getAllUsers() {
        return getUserList(() -> userStub.getAllUsers(Empty.newBuilder().build()), "all users");
    }

    public UserDto getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);
        try {

            UserIdRequest request = UserIdRequest.newBuilder()
                    .setId(userId)
                    .build();

            com.tripezzy.grpc.user.UserDto user = userStub
                    .getUserById(request);

            if (user == null) {
                log.warn("No user found with ID: {}", userId);
                return null;
            }
            log.info("User found with ID: {}", user.getId());
            return mapUser(user);
        } catch (StatusRuntimeException e) {
            handleGrpcException(e, "Failed to get user by ID: " + userId);
            return null;
        }
    }

    private List<UserDto> mapUserResponse(UserListResponse response) {
        return response.getUsersList().stream()
                .map(this::mapUser)
                .collect(Collectors.toUnmodifiableList());
    }

    private UserDto mapUser(com.tripezzy.grpc.user.UserDto user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setRole(user.getRole());
        log.info("Mapped user: {}", userDto);
        return userDto;
    }
}
