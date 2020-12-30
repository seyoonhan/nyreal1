package com.han.startup.exception;

import lombok.Getter;

@Getter
public class BackendException extends RuntimeException {
    protected ErrorType errorType;

    public BackendException(ErrorType errorType) {
        this(errorType, null);
    }

    public BackendException(ErrorType errorType, String message) {
        super(errorType.toString() + "[" + errorType.getErrorCode() + "] " + message);
        this.errorType = errorType;
    }
}
