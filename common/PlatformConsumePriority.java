package com.han.startup.common;

import com.ubisoft.hfx.mm.enumeration.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformConsumePriority {
    PlatformType platformType;
    @Builder.Default
    int priority = 0;
}
