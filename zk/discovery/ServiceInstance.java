package com.han.startup.zk.discovery;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
@Getter
@Setter
public class ServiceInstance<T> {
    private final String name;
    private final String id;
    private String address;
    private final Integer port;
    private final Integer sslPort;
    private final T payload;
    private final long registrationTimeUTC;
    private final ServiceType serviceType;
    private final UriSpec uriSpec;
    private final boolean enabled;

    public ServiceInstance(String name, String id, String address, Integer port, Integer sslPort, T payload, long registrationTimeUTC, ServiceType serviceType, UriSpec uriSpec) {
        this(name, id, address, port, sslPort, payload, registrationTimeUTC, serviceType, uriSpec, true);
    }

    /**
     * @param name                name of the service
     * @param id                  externalId of this instance (must be unique)
     * @param address             address of this instance
     * @param port                the port for this instance or null
     * @param sslPort             the SSL port for this instance or null
     * @param payload             the payload for this instance or null
     * @param registrationTimeUTC the time (in UTC) of the registration
     * @param serviceType         type of the service
     * @param uriSpec             the uri spec or null
     * @param enabled             true if the instance should be considered enabled
     */
    public ServiceInstance(String name, String id, String address, Integer port, Integer sslPort, T payload, long registrationTimeUTC, ServiceType serviceType, UriSpec uriSpec, boolean enabled) {
        name = Preconditions.checkNotNull(name, "name cannot be null");
        id = Preconditions.checkNotNull(id, "externalId cannot be null");

        this.serviceType = serviceType;
        this.uriSpec = uriSpec;
        this.name = name;
        this.id = id;
        this.address = address;
        this.port = port;
        this.sslPort = sslPort;
        this.payload = payload;
        this.registrationTimeUTC = registrationTimeUTC;
        this.enabled = enabled;
    }

    public ServiceInstance() {
        this("", "", null, null, null, null, 0, ServiceType.DYNAMIC, null, true);
    }

    public static <T> ServiceInstanceBuilder<T> builder() throws Exception {
//        String address = null;
//        Collection<InetAddress> ips = ConfigurationHolder.getAllLocalIPs();
//        if (ips.size() > 0) {
//            address = ips.iterator().next().getHostAddress();
//        }

        String id = UUID.randomUUID().toString();

        return new ServiceInstanceBuilder<T>().id(id).registrationTime(System.currentTimeMillis());
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = Object.class)
    public T getPayload() {
        return payload;
    }

    public String buildUriSpec() {
        return buildUriSpec(Maps.newHashMap());
    }

    public String buildUriSpec(Map<String, Object> variables) {
        return (uriSpec != null) ? uriSpec.build(this, variables) : "";
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (sslPort != null ? sslPort.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (int) (registrationTimeUTC ^ (registrationTimeUTC >>> 32));
        result = 31 * result + (serviceType != null ? serviceType.hashCode() : 0);
        result = 31 * result + (uriSpec != null ? uriSpec.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServiceInstance that = (ServiceInstance) o;

        if (registrationTimeUTC != that.registrationTimeUTC) {
            return false;
        }
        if (address != null ? !address.equals(that.address) : that.address != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (payload != null ? !payload.equals(that.payload) : that.payload != null) {
            return false;
        }
        if (port != null ? !port.equals(that.port) : that.port != null) {
            return false;
        }
        if (serviceType != that.serviceType) {
            return false;
        }
        if (sslPort != null ? !sslPort.equals(that.sslPort) : that.sslPort != null) {
            return false;
        }
        if (uriSpec != null ? !uriSpec.equals(that.uriSpec) : that.uriSpec != null) {
            return false;
        }
        if (enabled != that.enabled) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ServiceInstance{").append("name='").append(name)
                .append('\'').append(", externalId='").append(id).append('\'').append(", address='")
                .append(address).append('\'').append(", port=").append(port)
                .append(", sslPort=").append(sslPort).append(", payload=")
                .append(payload).append(", registrationTime=")
                .append(registrationTimeUTC).append(", serviceType=")
                .append(serviceType).append(", uriSpec=").append(uriSpec)
                .append(", enabled=").append(enabled).append('}').toString();
    }
}
