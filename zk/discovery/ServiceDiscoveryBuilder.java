package com.han.startup.zk.discovery;


import com.ubisoft.hfx.zk.discovery.details.InstanceSerializer;
import com.ubisoft.hfx.zk.discovery.details.JsonInstanceSerializer;
import com.ubisoft.hfx.zk.discovery.details.ServiceDiscoveryImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
public class ServiceDiscoveryBuilder<T> {
	private CuratorFramework client;
	private String basePath;
	private InstanceSerializer<T> serializer;
	private ServiceInstance<T> thisInstance;
	private Class<T> payloadClass;
	private boolean watchInstances = false;

	ServiceDiscoveryBuilder(Class<T> payloadClass) {
		this.payloadClass = payloadClass;
	}

	public static <T> ServiceDiscoveryBuilder<T> builder(Class<T> payloadClass) {
		return new ServiceDiscoveryBuilder<T>(payloadClass);
	}

	public ServiceDiscovery<T> build() {
		if (serializer == null) {
			serializer(new JsonInstanceSerializer<T>(payloadClass));
		}
		return new ServiceDiscoveryImpl<T>(client, basePath, serializer, thisInstance, watchInstances);
	}

	public ServiceDiscoveryBuilder<T> serializer(InstanceSerializer<T> serializer) {
		this.serializer = serializer;
		return this;
	}

	public ServiceDiscoveryBuilder<T> client(CuratorFramework client) {
		this.client = client;
		return this;
	}

	public ServiceDiscoveryBuilder<T> basePath(String basePath) {
		this.basePath = basePath;
		return this;
	}

	public ServiceDiscoveryBuilder<T> thisInstance(ServiceInstance<T> thisInstance) {
		this.thisInstance = thisInstance;
		return this;
	}

	public ServiceDiscoveryBuilder<T> watchInstances(boolean watchInstances) {
		this.watchInstances = watchInstances;
		return this;
	}
}
