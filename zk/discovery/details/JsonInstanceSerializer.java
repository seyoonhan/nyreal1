package com.han.startup.zk.discovery.details;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubisoft.hfx.zk.discovery.ServiceInstance;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
public class JsonInstanceSerializer<T> implements InstanceSerializer<T> {
	private final ObjectMapper mapper;
	private final Class<T> payloadClass;
	private final JavaType type;

	public JsonInstanceSerializer(Class<T> payloadClass) {
		this.payloadClass = payloadClass;
		mapper = new ObjectMapper();
		type = mapper.getTypeFactory().constructType(ServiceInstance.class);
	}

	@Override
	public byte[] serialize(ServiceInstance<T> instance) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mapper.writeValue(out, instance);
		return out.toByteArray();
	}

	@Override
	public ServiceInstance<T> deserialize(byte[] bytes) throws Exception {
		ServiceInstance rawServiceInstance = mapper.readValue(bytes, type);
		payloadClass.cast(rawServiceInstance.getPayload());
		return (ServiceInstance<T>) rawServiceInstance;
	}
}
