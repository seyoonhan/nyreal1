package com.han.startup.zk.discovery;

import org.apache.curator.utils.CloseableExecutorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface ServiceCacheBuilder<T> {
	ServiceCache<T> build();

	ServiceCacheBuilder<T> name(String name);

	ServiceCacheBuilder<T> threadFactory(ThreadFactory threadFactory);

	ServiceCacheBuilder<T> executorService(ExecutorService executorService);

	ServiceCacheBuilder<T> executorService(CloseableExecutorService executorService);
}
