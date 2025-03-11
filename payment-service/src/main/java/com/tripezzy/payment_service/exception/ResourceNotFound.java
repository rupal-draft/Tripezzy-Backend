package com.tripezzy.payment_service.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFound extends RuntimeException {
    private final HttpStatus status;

    public ResourceNotFound(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
