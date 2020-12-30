package com.han.startup.exception;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class FailureResponse implements Serializable {
    private long timestamp;
    private int errorCode;
    private String errorMessage;
}
