package com.tripezzy.uploader_service.exceptions;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
