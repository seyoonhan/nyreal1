package com.han.startup.zk.discovery.details;


import com.ubisoft.hfx.zk.discovery.ServiceInstance;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface InstanceSerializer<T> {
	byte[] serialize(ServiceInstance<T> instance) throws Exception;

	ServiceInstance<T> deserialize(byte[] bytes) throws Exception;
}
