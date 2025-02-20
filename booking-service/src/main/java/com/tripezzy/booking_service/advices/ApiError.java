package com.tripezzy.booking_service.advices;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ApiError {
    private final HttpStatus status;
    private final String message;
    private final List<String> subErrors;

    private ApiError(ApiErrorBuilder builder) {
        this.status = Objects.requireNonNull(builder.status, "Status cannot be null");
        this.message = Objects.requireNonNull(builder.message, "Message cannot be null");
        this.subErrors = builder.subErrors != null ? Collections.unmodifiableList(builder.subErrors) : Collections.emptyList();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getSubErrors() {
        return subErrors;
    }

    public static class ApiErrorBuilder {
        private HttpStatus status;
        private String message;
        private List<String> subErrors;

        public ApiErrorBuilder setStatus(HttpStatus status) {
            this.status = status;
            return this;
        }

        public ApiErrorBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public ApiErrorBuilder setSubErrors(List<String> subErrors) {
            this.subErrors = subErrors;
            return this;
        }

        public ApiError build() {
            return new ApiError(this);
        }
    }
}