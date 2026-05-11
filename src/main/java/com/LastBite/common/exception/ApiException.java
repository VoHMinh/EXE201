package com.LastBite.common.exception;

import lombok.Getter;

/**
 * Application-level business exception.
 * <p>
 * Thrown anywhere in the service layer when a known error condition occurs.
 * Caught globally by {@link GlobalExceptionHandler}.
 */
@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
