package com.han.startup.zk.discovery.details;

import com.ubisoft.hfx.zk.discovery.ServiceCache;
import com.ubisoft.hfx.zk.discovery.ServiceCacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.utils.CloseableExecutorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
class ServiceCacheBuilderImpl<T> implements ServiceCacheBuilder<T> {
	private ServiceDiscoveryImpl<T> discovery;
	private String name;
	private ThreadFactory threadFactory;
	private CloseableExecutorService executorService;

	ServiceCacheBuilderImpl(ServiceDiscoveryImpl<T> discovery) {
		this.discovery = discovery;
	}

	@Override
	public ServiceCache<T> build() {
		if (executorService != null) {
			return new ServiceCacheImpl<>(discovery, name, executorService);
		} else {
			return new ServiceCacheImpl<>(discovery, name, threadFactory);
		}
	}

	@Override
	public ServiceCacheBuilder<T> name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public ServiceCacheBuilder<T> threadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
		this.executorService = null;
		return this;
	}

	@Override
	public ServiceCacheBuilder<T> executorService(ExecutorService executorService) {
		this.executorService = new CloseableExecutorService(executorService);
		this.threadFactory = null;
		return this;
	}

	@Override
	public ServiceCacheBuilder<T> executorService(CloseableExecutorService executorService) {
		this.executorService = executorService;
		this.threadFactory = null;
		return this;
	}
}
