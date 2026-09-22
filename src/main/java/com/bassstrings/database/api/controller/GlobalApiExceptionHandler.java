package com.bassstrings.database.api.controller;

import com.bassstrings.database.api.model.ApiError;
import com.bassstrings.database.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Note: {@link org.springframework.security.access.AccessDeniedException} is deliberately
 * NOT handled here - it's left to propagate to Spring Security's {@code ExceptionTranslationFilter}
 * so the API-vs-browser distinction configured in {@code SecurityConfig}'s
 * {@code RestAwareAccessDeniedHandler} stays the single source of truth for 403 responses.
 */
@RestControllerAdvice(basePackages = "com.bassstrings.hub.api")
public class GlobalApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error(HttpStatus.BAD_REQUEST, message.isBlank() ? "Invalid request" : message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    private ApiError error(HttpStatus status, String message) {
        return new ApiError().status(status.value()).message(message).timestamp(OffsetDateTime.now());
    }
}
