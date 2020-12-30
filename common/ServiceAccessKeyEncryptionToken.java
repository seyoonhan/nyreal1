package com.han.startup.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceAccessKeyEncryptionToken {
    boolean activated;
    String keyString;
    long updatedAt;
}
