package com.tripezzy.uploader_service.exceptions;

public class FileSizeExceededException extends RuntimeException {
    public FileSizeExceededException(String message) {
        super(message);
    }
}
