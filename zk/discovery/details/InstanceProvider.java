package com.han.startup.zk.discovery.details;

import com.ubisoft.hfx.zk.discovery.ServiceInstance;

import java.util.List;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface InstanceProvider<T> {
	List<ServiceInstance<T>> getInstances() throws Exception;
}
