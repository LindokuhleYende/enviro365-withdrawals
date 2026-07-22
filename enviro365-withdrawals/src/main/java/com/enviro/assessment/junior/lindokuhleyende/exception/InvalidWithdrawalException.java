package com.enviro.assessment.junior.lindokuhleyende.exception;

/**
 * Thrown when a withdrawal request violates a business rule
 * (e.g. exceeds balance, exceeds the 90% cap, or fails the retirement age rule).
 * Mapped to HTTP 400 by the GlobalExceptionHandler.
 */
public class InvalidWithdrawalException extends RuntimeException {

    public InvalidWithdrawalException(String message) {
        super(message);
    }
}
