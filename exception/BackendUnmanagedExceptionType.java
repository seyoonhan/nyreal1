package com.han.startup.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BackendUnmanagedExceptionType implements ErrorType {

    ZK_NODE_CREATION_FAIL(110003),
    REDIS_CORRUPTED_VALUE(110050),

    DUPLICATE_DISPLAY_NAME(110101),
    INVALID_ACCESS_KEY(110201),
    INVALID_ARGUMENTS(110202),

    //mm
    MM_SERVICE_NOT_ASSIGNED(110301),

    //ss
    SS_GROUP_CODE_GENERATION_FAIL(110401),

    UNKNOWN_ERROR(199999),
    ;

    BackendUnmanagedExceptionType(int errorCode) {
        this(errorCode, ErrorLogLevel.ERROR);
    }

    BackendUnmanagedExceptionType(int errorCode, ErrorLogLevel errorLogLevel) {
        this(errorCode, errorLogLevel, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private int errorCode;
    private ErrorLogLevel errorLogLevel;
    private HttpStatus status;
}
