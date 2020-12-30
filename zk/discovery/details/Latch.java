package com.han.startup.zk.discovery.details;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
class Latch {
	private volatile boolean laden = false;

	synchronized void set() {
		laden = true;
		notifyAll();
	}

	synchronized void await() throws InterruptedException {
		while (!laden) {
			wait();
		}
		laden = false;
	}
}
