package com.han.startup.zk.discovery;

import com.ubisoft.hfx.zk.model.ConfigurationHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
public class ServiceInstanceBuilder<T> {

	private T payload;
	private String name;
	private String address;
	private Integer port;
	private Integer sslPort;
	private String id;
	private long registrationTime;
	private ServiceType serviceType = ServiceType.DYNAMIC;
	private UriSpec uriSpec;
	private boolean enabled = true;

	ServiceInstanceBuilder() {
	}

	public static LocalIpFilter getLocalIpFilter() {
		return ConfigurationHolder.localIpFilter.get();
	}

	public static void setLocalIpFilter(LocalIpFilter newLocalIpFilter) {
		ConfigurationHolder.localIpFilter.set(newLocalIpFilter);
	}

	public ServiceInstance<T> build() {
		return new ServiceInstance<>(name, id, address, port, sslPort, payload, registrationTime, serviceType, uriSpec, enabled);
	}

	public ServiceInstanceBuilder<T> name(String name) {
		this.name = name;
		return this;
	}

	public ServiceInstanceBuilder<T> address(String address) {
		this.address = address;
		return this;
	}

	public ServiceInstanceBuilder<T> id(String id) {
		this.id = id;
		return this;
	}

	public ServiceInstanceBuilder<T> port(int port) {
		this.port = port;
		return this;
	}

	public ServiceInstanceBuilder<T> sslPort(int port) {
		this.sslPort = port;
		return this;
	}

	public ServiceInstanceBuilder<T> payload(T payload) {
		this.payload = payload;
		return this;
	}

	public ServiceInstanceBuilder<T> serviceType(ServiceType serviceType) {
		this.serviceType = serviceType;
		return this;
	}

	public ServiceInstanceBuilder<T> registrationTime(long registrationTime) {
		this.registrationTime = registrationTime;
		return this;
	}

	public ServiceInstanceBuilder<T> uriSpec(UriSpec uriSpec) {
		this.uriSpec = uriSpec;
		return this;
	}

	public ServiceInstanceBuilder<T> enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}
}
