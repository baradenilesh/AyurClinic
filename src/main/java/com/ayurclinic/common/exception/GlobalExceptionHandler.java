package com.ayurclinic.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleEntityNotFound(
            EntityNotFoundException exception,
            HttpServletRequest request) {

        return ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(404)
                .error("NOT_FOUND")
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        return ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(404)
                .error("NOT_FOUND")
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(400)
                .error("BAD_REQUEST")
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(400)
                .error("VALIDATION_ERROR")
                .message("Validation failed")
                .path(request.getRequestURI())
                .errors(errors)
                .build();
    }
}