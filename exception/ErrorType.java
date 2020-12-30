package com.han.startup.exception;

import org.springframework.http.HttpStatus;

public interface ErrorType {
    int getErrorCode();
    ErrorLogLevel getErrorLogLevel();
    HttpStatus getStatus();
}
