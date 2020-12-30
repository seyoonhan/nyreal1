package com.han.startup.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BackendManagedExceptionType implements ErrorType {
    //GENERAL
    UNIMPLEMENTED(00001),

    //orchestration 10000~
    RESOURCE_NOT_AVAILABLE(10001),
    DUPLICATE_RESOURCE(10002),
    DUPLICATE_DISPLAY_NAME(10101),
    INVALID_ACCESS_KEY(10201),
    RESOURCE_EXPIRED(10301),

    //matchmaking 20000~
    INVALID_CL(20001),
    NOT_AVAILABLE_FOR_MM_TYPE(20100),
    NO_AVAILABLE_MATCHING_WORKER(20101),
    FREQUENT_ACCESS(20200),
    SERVICE_UNDER_HIGH_LOAD(20300),
    NO_MATCHING_RULE_CONFIGURED(20400),

    CODE_SHARE_MATCH_ROOM_FULL(20400),
    CODE_SHARE_MATCH_ROOM_NOT_EXIST(20401)
    ;

    BackendManagedExceptionType(int errorCode) {
        this(errorCode, ErrorLogLevel.ERROR);
    }

    BackendManagedExceptionType(int errorCode, ErrorLogLevel errorLogLevel) {
        this(errorCode, errorLogLevel, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private int errorCode;
    private ErrorLogLevel errorLogLevel;
    private HttpStatus status;
}
