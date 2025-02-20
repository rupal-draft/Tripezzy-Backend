package com.tripezzy.booking_service.advices;

import java.time.LocalDateTime;
import java.util.Objects;

public final class ApiResponse<T> {
    private final LocalDateTime timeStamp;
    private final ApiError error;
    private final T data;
    private final boolean success;

    private ApiResponse(T data, ApiError error, boolean success) {
        this.timeStamp = LocalDateTime.now();
        this.data = data;
        this.error = error;
        this.success = success;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(Objects.requireNonNull(data, "Data cannot be null"), null, true);
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return new ApiResponse<>(null, Objects.requireNonNull(error, "Error cannot be null"), false);
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public ApiError getError() {
        return error;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }
}