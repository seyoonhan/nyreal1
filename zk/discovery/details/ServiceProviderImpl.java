package com.han.startup.zk.discovery.details;

import com.google.common.collect.Lists;
import com.ubisoft.hfx.zk.discovery.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
public class ServiceProviderImpl<T> implements ServiceProvider<T> {
	private final ServiceCache<T> cache;
	private final InstanceProvider<T> instanceProvider;
	private final ServiceDiscoveryImpl<T> discovery;
	private final ProviderStrategy<T> providerStrategy;
	private final DownInstanceManager<T> downInstanceManager;

	public ServiceProviderImpl(ServiceDiscoveryImpl<T> discovery, String serviceName, ProviderStrategy<T> providerStrategy, ThreadFactory threadFactory, List<InstanceFilter<T>> filters, DownInstancePolicy downInstancePolicy) {
		this.discovery = discovery;
		this.providerStrategy = providerStrategy;

		downInstanceManager = new DownInstanceManager<T>(downInstancePolicy);
		cache = discovery.serviceCacheBuilder().name(serviceName).threadFactory(threadFactory).build();

		ArrayList<InstanceFilter<T>> localFilters = Lists.newArrayList(filters);
		localFilters.add(downInstanceManager);
		localFilters.add(instance -> instance.isEnabled());
		instanceProvider = new FilteredInstanceProvider<T>(cache, localFilters);
	}

	@Override
	public void start() throws Exception {
		cache.start();
		discovery.providerOpened(this);
	}

	@Override
	public ServiceInstance<T> getInstance() throws Exception {
		return providerStrategy.getInstance(instanceProvider);
	}

	@Override
	public Collection<ServiceInstance<T>> getAllInstances() throws Exception {
		return instanceProvider.getInstances();
	}

	@Override
	public void noteError(ServiceInstance<T> instance) {
		downInstanceManager.add(instance);
	}

	@Override
	public void close() throws IOException {
		discovery.providerClosed(this);
		cache.close();
	}
}
