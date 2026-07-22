package com.enviro.assessment.junior.lindokuhleyende.exception;

/**
 * Thrown when a requested entity (Investor, Product, WithdrawalNotice) cannot be found.
 * Mapped to HTTP 404 by the GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
