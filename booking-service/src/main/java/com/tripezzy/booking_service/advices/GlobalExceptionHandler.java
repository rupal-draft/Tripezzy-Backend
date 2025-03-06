package com.tripezzy.booking_service.advices;

import com.tripezzy.booking_service.exception.IllegalState;
import com.tripezzy.booking_service.exception.ResourceNotFound;
import com.tripezzy.booking_service.exception.RuntimeConflict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFound exception) {
        logger.error("Resource not found: {}", exception.getMessage());
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .setStatus(HttpStatus.NOT_FOUND)
                .setMessage(exception.getLocalizedMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(RuntimeConflict.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeConflictException(RuntimeConflict exception) {
        logger.error("Runtime conflict: {}", exception.getMessage());
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .setStatus(HttpStatus.CONFLICT)
                .setMessage(exception.getLocalizedMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(IllegalState.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalState exception) {
        logger.error("Illegal state: {}", exception.getMessage());
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .setStatus(HttpStatus.BAD_REQUEST)
                .setMessage(exception.getLocalizedMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        logger.error("Validation failed: {}", errors);
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .setStatus(HttpStatus.BAD_REQUEST)
                .setMessage("Input validation failed!")
                .setSubErrors(errors)
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        logger.error("Constraint violation: {}", errors);
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .setStatus(HttpStatus.BAD_REQUEST)
                .setMessage("Constraint validation failed!")
                .setSubErrors(errors)
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleInternalServerErrorException(Exception exception) {
        logger.error("Internal server error: {}", exception.getMessage(), exception);
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .setMessage("An unexpected error occurred. Please try again later.")
                .build();
        return buildErrorResponseEntity(apiError);
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(ApiResponse.error(apiError), apiError.getStatus());
    }
}