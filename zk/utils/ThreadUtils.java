package com.han.startup.zk.utils;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Created by shan2 on 7/7/2017.
 */
public class ThreadUtils {
	private static final Logger log = LoggerFactory.getLogger(org.apache.curator.utils.ThreadUtils.class);

	public static void checkInterrupted(Throwable e) {
		if (e instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
	}

	public static ExecutorService newSingleThreadExecutor(String processName) {
		return Executors.newSingleThreadExecutor(newThreadFactory(processName));
	}

	public static ThreadFactory newThreadFactory(String processName) {
		return newGenericThreadFactory("Curator-" + processName);
	}

	public static ThreadFactory newGenericThreadFactory(String processName) {
		Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (t, e) -> {
			log.error("Unexpected exception in thread: " + t, e);
			Throwables.propagate(e);
		};
		return new ThreadFactoryBuilder()
				.setNameFormat(processName + "-%d")
				.setDaemon(true)
				.setUncaughtExceptionHandler(uncaughtExceptionHandler)
				.build();
	}

	public static ExecutorService newFixedThreadPool(int qty, String processName) {
		return Executors.newFixedThreadPool(qty, newThreadFactory(processName));
	}

	public static ScheduledExecutorService newSingleThreadScheduledExecutor(String processName) {
		return Executors.newSingleThreadScheduledExecutor(newThreadFactory(processName));
	}

	public static ScheduledExecutorService newFixedThreadScheduledPool(int qty, String processName) {
		return Executors.newScheduledThreadPool(qty, newThreadFactory(processName));
	}

	public static String getProcessName(Class<?> clazz) {
		if (clazz.isAnonymousClass()) {
			return getProcessName(clazz.getEnclosingClass());
		}
		return clazz.getSimpleName();
	}
}
