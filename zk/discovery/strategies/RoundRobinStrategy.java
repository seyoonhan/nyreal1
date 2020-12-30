package com.han.startup.zk.discovery.strategies;

import com.ubisoft.hfx.zk.discovery.ProviderStrategy;
import com.ubisoft.hfx.zk.discovery.ServiceInstance;
import com.ubisoft.hfx.zk.discovery.details.InstanceProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
public class RoundRobinStrategy<T> implements ProviderStrategy<T> {
	private final AtomicInteger index = new AtomicInteger(0);

	@Override
	public ServiceInstance<T> getInstance(InstanceProvider<T> instanceProvider) throws Exception {
		List<ServiceInstance<T>> instances = instanceProvider.getInstances();
		if (instances.size() == 0) {
			return null;
		}
		int thisIndex = Math.abs(index.getAndIncrement());
		return instances.get(thisIndex % instances.size());
	}
}
