package com.tripezzy.notification_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ServiceUnavailable extends ResponseStatusException {
    public ServiceUnavailable(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
