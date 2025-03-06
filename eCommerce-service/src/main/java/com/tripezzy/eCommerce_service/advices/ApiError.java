package com.tripezzy.eCommerce_service.advices;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ApiError {

    private final String message;
    private final HttpStatus status;
    private final List<String> subErrors;

    public ApiError(ApiErrorBuilder builder) {
        this.message = Objects.requireNonNull(builder.message, "Message cannot be null");
        this.status = Objects.requireNonNull(builder.status, "Status cannot be null");
        this.subErrors = builder.subErrors != null ?
                Collections.unmodifiableList(builder.subErrors) : Collections.emptyList();
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getSubErrors() {
        return subErrors;
    }

    public static class ApiErrorBuilder {
        private String message;
        private HttpStatus status;
        private List<String> subErrors;

        public ApiErrorBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public ApiErrorBuilder setStatus(HttpStatus status) {
            this.status = status;
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
