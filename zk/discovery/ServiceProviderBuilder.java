package com.han.startup.zk.discovery;

import java.util.concurrent.ThreadFactory;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface ServiceProviderBuilder<T> {
	ServiceProvider<T> build();

	ServiceProviderBuilder<T> serviceName(String serviceName);

	ServiceProviderBuilder<T> providerStrategy(ProviderStrategy<T> providerStrategy);

	ServiceProviderBuilder<T> threadFactory(ThreadFactory threadFactory);

	ServiceProviderBuilder<T> downInstancePolicy(DownInstancePolicy downInstancePolicy);

	ServiceProviderBuilder<T> additionalFilter(InstanceFilter<T> filter);
}
