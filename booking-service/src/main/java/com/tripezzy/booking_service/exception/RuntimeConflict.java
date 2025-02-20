package com.tripezzy.booking_service.exception;

public class RuntimeConflict extends RuntimeException {
    public RuntimeConflict(String message) {
        super(message);
    }
}
