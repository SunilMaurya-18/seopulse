package com.seopulse.common.exception;

public class DuplicateResourceException extends RuntimeException {
    private DuplicateResourceException(String message) {
        super(message);
    }

}
