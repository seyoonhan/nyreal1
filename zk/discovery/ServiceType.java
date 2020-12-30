package com.han.startup.zk.discovery;

/**
 * Created by shan2 on 7/18/2017.
 */
public enum ServiceType {
	DYNAMIC,
	STATIC,
	PERMANENT,
	DYNAMIC_SEQUENTIAL;

	public boolean isDynamic() {
		return this == DYNAMIC || this == DYNAMIC_SEQUENTIAL;
	}
}
