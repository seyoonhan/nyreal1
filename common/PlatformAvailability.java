package com.han.startup.common;

import com.ubisoft.hfx.mm.enumeration.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformAvailability {
    @Getter
    PlatformType platformType;
    @Getter
    @Builder.Default
    boolean enabled = false;
}
