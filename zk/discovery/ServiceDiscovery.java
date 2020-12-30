package com.han.startup.zk.discovery;

import java.io.Closeable;
import java.util.Collection;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface ServiceDiscovery<T> extends Closeable {
	void start() throws Exception;

	void registerService(ServiceInstance<T> service) throws Exception;

	void updateService(ServiceInstance<T> service) throws Exception;

	void unregisterService(ServiceInstance<T> service) throws Exception;

	ServiceCacheBuilder<T> serviceCacheBuilder();

	Collection<String> queryForNames() throws Exception;

	Collection<ServiceInstance<T>> queryForInstances(String name) throws Exception;

	ServiceInstance<T> queryForInstance(String name, String id) throws Exception;

	ServiceProviderBuilder<T> serviceProviderBuilder();
}
