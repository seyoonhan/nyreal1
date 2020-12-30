package com.han.startup.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralErrorType implements ErrorType {
    // FIXME: Organize code
    GENERAL_UNAUTHORIZED(4, ErrorLogLevel.WARN),
    GENERAL_NO_PERMISSION(5, ErrorLogLevel.WARN),
    GENERAL_JSON_DESERIALIZATION_FAIL(6),
    UNKNOWN_ERROR(100),
    UNKNOWN_HTTP_CLIENT_ERROR(101),
    ;

    GeneralErrorType(int errorCode) {
        this(errorCode, ErrorLogLevel.ERROR);
    }

    GeneralErrorType(int errorCode, ErrorLogLevel errorLogLevel) {
        this(errorCode, errorLogLevel, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private int errorCode;
    private ErrorLogLevel errorLogLevel;
    private HttpStatus status;
}
