package com.han.startup.zk.discovery;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
public class DownInstancePolicy {
	private static final long DEFAULT_TIMEOUT_MS = 30000;
	private static final int DEFAULT_THRESHOLD = 2;
	private final long timeoutMs;
	private final int errorThreshold;

	public DownInstancePolicy() {
		this(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS, DEFAULT_THRESHOLD);
	}

	public DownInstancePolicy(long timeout, TimeUnit unit, int errorThreshold) {
		this.timeoutMs = unit.toMillis(timeout);
		this.errorThreshold = errorThreshold;
	}

	public long getTimeoutMs() {
		return timeoutMs;
	}

	public int getErrorThreshold() {
		return errorThreshold;
	}
}
