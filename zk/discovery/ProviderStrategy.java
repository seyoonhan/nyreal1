package com.han.startup.zk.discovery;

import com.ubisoft.hfx.zk.discovery.details.InstanceProvider;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface ProviderStrategy<T> {
	ServiceInstance<T> getInstance(InstanceProvider<T> instanceProvider) throws Exception;
}
