package com.tripezzy.user_service.grpc;

import com.google.protobuf.Empty;
import com.tripezzy.grpc.user.SingleUserResponse;
import com.tripezzy.grpc.user.UserIdRequest;
import com.tripezzy.grpc.user.UserListResponse;
import com.tripezzy.grpc.user.UserServiceGrpc;
import com.tripezzy.user_service.dto.UserDto;
import com.tripezzy.user_service.service.AuthService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(UserGrpcService.class);
    private final AuthService authService;

    public UserGrpcService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void getAllUsers(Empty request, StreamObserver<UserListResponse> responseObserver) {
        handleUserListRequest(responseObserver, authService::getAllUsers, "getAllUsers");
    }

    @Override
    public void getAllAdminUsers(Empty request, StreamObserver<UserListResponse> responseObserver) {
        handleUserListRequest(responseObserver, authService::getAllAdminUsers, "getAllAdminUsers");
    }

    @Override
    public void getAllSellerUsers(Empty request, StreamObserver<UserListResponse> responseObserver) {
        handleUserListRequest(responseObserver, authService::getAllSellerUsers, "getAllSellerUsers");
    }

    @Override
    public void getAllGuideUsers(Empty request, StreamObserver<UserListResponse> responseObserver) {
        handleUserListRequest(responseObserver, authService::getAllGuideUsers, "getAllGuideUsers");
    }

    @Override
    public void getUserById(UserIdRequest request, StreamObserver<com.tripezzy.grpc.user.UserDto> responseObserver) {
        try {
            log.info("Processing gRPC request for getUserById with ID: {}", request.getId());
            UserDto user = authService.getUserById(request.getId());
            log.info("Sending response: {}", user);
            responseObserver.onNext(mapToGrpcUser(user));
            responseObserver.onCompleted();
        } catch (Exception e) {
            handleError(responseObserver, e, "getUserById");
        }
    }

    private void handleUserListRequest(StreamObserver<UserListResponse> responseObserver,
                                       Supplier<List<UserDto>> serviceMethod,
                                       String methodName) {
        try {
            log.info("Processing gRPC request for {}", methodName);
            List<UserDto> users = serviceMethod.get();
            List<com.tripezzy.grpc.user.UserDto> grpcUsers = users
                    .stream()
                    .map(this::mapToGrpcUser)
                    .collect(Collectors.toUnmodifiableList());

            UserListResponse response = UserListResponse.newBuilder()
                    .addAllUsers(grpcUsers)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            handleError(responseObserver, e, methodName);
        }
    }

    private void handleError(StreamObserver<?> responseObserver, Exception e, String methodName) {
        log.error("Error in {}: {}", methodName, e.getMessage(), e);
        StatusRuntimeException statusException = Status.INTERNAL
                .withDescription("Internal server error in " + methodName)
                .withCause(e)
                .asRuntimeException();
        responseObserver.onError(statusException);
    }

    private com.tripezzy.grpc.user.UserDto mapToGrpcUser(UserDto user) {
        return com.tripezzy.grpc.user.UserDto.newBuilder()
                .setId(user.getId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmail(user.getEmail())
                .setRole(user.getRole())
                .setPhoneNumber(user.getPhoneNumber())
                .build();
    }
}
