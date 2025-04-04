package com.tripezzy.payment_service.exceptions;

public class RuntimeConflict extends RuntimeException {
    public RuntimeConflict(String message) {
        super(message);
    }
}
