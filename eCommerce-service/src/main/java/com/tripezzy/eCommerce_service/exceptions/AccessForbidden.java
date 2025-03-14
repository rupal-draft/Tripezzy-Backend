package com.tripezzy.eCommerce_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AccessForbidden extends ResponseStatusException {
    public AccessForbidden(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
