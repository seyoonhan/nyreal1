package com.han.startup.zk.discovery.details;

import com.google.common.collect.Lists;
import com.ubisoft.hfx.zk.discovery.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
class ServiceProviderBuilderImpl<T> implements ServiceProviderBuilder<T> {
	private ServiceDiscoveryImpl<T> discovery;
	private String serviceName;
	private ProviderStrategy<T> providerStrategy;
	private ThreadFactory threadFactory;
	private List<InstanceFilter<T>> filters = Lists.newArrayList();
	private DownInstancePolicy downInstancePolicy = new DownInstancePolicy();

	ServiceProviderBuilderImpl(ServiceDiscoveryImpl<T> discovery) {
		this.discovery = discovery;
	}

	public ServiceProvider<T> build() {
		return new ServiceProviderImpl<T>(discovery, serviceName, providerStrategy, threadFactory, filters, downInstancePolicy);
	}

	@Override
	public ServiceProviderBuilder<T> serviceName(String serviceName) {
		this.serviceName = serviceName;
		return this;
	}

	@Override
	public ServiceProviderBuilder<T> providerStrategy(ProviderStrategy<T> providerStrategy) {
		this.providerStrategy = providerStrategy;
		return this;
	}

	@Override
	public ServiceProviderBuilder<T> threadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
		return this;
	}

	@Override
	public ServiceProviderBuilder<T> downInstancePolicy(DownInstancePolicy downInstancePolicy) {
		this.downInstancePolicy = downInstancePolicy;
		return this;
	}

	@Override
	public ServiceProviderBuilder<T> additionalFilter(InstanceFilter<T> filter) {
		filters.add(filter);
		return this;
	}
}
