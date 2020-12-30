package com.han.startup.zk.discovery;


import java.io.Closeable;
import java.util.Collection;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface ServiceProvider<T> extends Closeable {
	void start() throws Exception;

	ServiceInstance<T> getInstance() throws Exception;

	Collection<ServiceInstance<T>> getAllInstances() throws Exception;

	void noteError(ServiceInstance<T> instance);
}
