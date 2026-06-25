package com.payroll.reconciliation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Global exception handler for REST API controllers.
 * Translates common exceptions into appropriate HTTP responses.
 *
 * @author Payroll Reconciliation Team
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles {@link NoSuchElementException} by returning a 404 Not Found response.
     *
     * @return response entity with a not-found message
     */
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Record not found"));
    }

    /**
     * Handles {@link IllegalArgumentException} and {@link MethodArgumentNotValidException}
     * by returning a 400 Bad Request response.
     *
     * @param ex the thrown exception
     * @return response entity with the error message
     */
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<Map<String, String>> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
