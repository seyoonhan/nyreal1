package com.han.startup.zk.discovery.strategies;


import com.ubisoft.hfx.zk.discovery.ProviderStrategy;
import com.ubisoft.hfx.zk.discovery.ServiceInstance;
import com.ubisoft.hfx.zk.discovery.details.InstanceProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
public class StickyStrategy<T> implements ProviderStrategy<T> {
	private final ProviderStrategy<T> masterStrategy;
	private final AtomicReference<ServiceInstance<T>> ourInstance = new AtomicReference<>(null);
	private final AtomicInteger instanceNumber = new AtomicInteger(-1);

	public StickyStrategy(ProviderStrategy<T> masterStrategy) {
		this.masterStrategy = masterStrategy;
	}

	@Override
	public ServiceInstance<T> getInstance(InstanceProvider<T> instanceProvider) throws Exception {
		final List<ServiceInstance<T>> instances = instanceProvider.getInstances();

		{
			ServiceInstance<T> localOurInstance = ourInstance.get();
			if (!instances.contains(localOurInstance)) {
				ourInstance.compareAndSet(localOurInstance, null);
			}
		}

		if (ourInstance.get() == null) {
			ServiceInstance<T> instance = masterStrategy.getInstance
					(
							() -> instances
					);
			if (ourInstance.compareAndSet(null, instance)) {
				instanceNumber.incrementAndGet();
			}
		}
		return ourInstance.get();
	}

	public int getInstanceNumber() {
		return instanceNumber.get();
	}
}
