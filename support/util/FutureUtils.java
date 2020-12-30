package com.han.startup.support.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class FutureUtils {

    public static <T extends Object> T get(CompletableFuture<T> future, long timeOutMillis) {
        T result = null;
        try {
            result = future.get(timeOutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage(), e);
        } catch (TimeoutException e) {
            log.error(e.getMessage() + ", timeout!", e);
        }

        return result;
    }
}
