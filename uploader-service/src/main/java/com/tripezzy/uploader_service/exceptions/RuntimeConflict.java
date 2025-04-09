package com.tripezzy.uploader_service.exceptions;

public class RuntimeConflict extends RuntimeException {
    public RuntimeConflict(String message) {
        super(message);
    }
}
