package com.LastBite.common.exception;

import lombok.Getter;

/**
 * Exception nghiệp vụ cấp ứng dụng.
 * <p>
 * Được ném trong service layer khi gặp lỗi đã biết.
 * Được bắt toàn cục bởi {@link GlobalExceptionHandler}.
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
