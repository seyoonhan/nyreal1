package com.han.startup.common.annotation;

import com.ubisoft.hfx.mm.enumeration.MatchType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedMatchType {
    MatchType value();
}
