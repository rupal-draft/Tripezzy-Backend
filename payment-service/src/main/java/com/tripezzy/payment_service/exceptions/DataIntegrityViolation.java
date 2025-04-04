package com.tripezzy.payment_service.exceptions;

import org.springframework.dao.DataIntegrityViolationException;

public class DataIntegrityViolation extends DataIntegrityViolationException {
    public DataIntegrityViolation(String message) {
        super(message);
    }
}
