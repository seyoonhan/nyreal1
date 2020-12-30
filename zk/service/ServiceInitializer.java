package com.han.startup.zk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.ubisoft.hfx.common.ServerHealth;
import com.ubisoft.hfx.zk.discovery.ServiceDiscovery;
import com.ubisoft.hfx.zk.discovery.ServiceDiscoveryBuilder;
import com.ubisoft.hfx.zk.discovery.ServiceInstance;
import com.ubisoft.hfx.zk.discovery.details.JsonInstanceSerializer;
import com.ubisoft.hfx.zk.discovery.listener.ServiceDiscoveryUpdateListener;
import com.ubisoft.hfx.zk.model.ConfigurationHolder;
import com.ubisoft.hfx.zk.model.InstanceDetails;
import com.ubisoft.hfx.zk.utils.CloseableUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

/**
 * Created by shan2 on 7/20/2017.
 */
@Slf4j
public class ServiceInitializer implements Closeable {
    @Getter
    private ServiceDiscovery<InstanceDetails> serviceDiscovery;
    @Getter
    private ServiceInstance<InstanceDetails> thisInstance;

    private CuratorFramework zkClient;
    private String basePath;
    private TreeCache sdTreeCashe;
    private ObjectMapper objectMapper;
    private ServerHealth serverHealth;
    @Getter
    List<ServiceDiscoveryUpdateListener> sdUpdateListener;

    public ServiceInitializer(CuratorFramework client,
                              String address,
                              String path,
                              String serviceName,
                              int port,
                              InstanceDetails instanceDetails,
                              ObjectMapper objectMapper,
                              ServerHealth serverHealth) throws Exception {
        thisInstance = ServiceInstance.<InstanceDetails>builder()
                .address(address)
                .name(serviceName)
                .payload(instanceDetails)
                .port(port)
                .registrationTime(DateTime.now().getMillis())
                .enabled(true)
                .build();

        this.zkClient = client;
        this.basePath = path;
        this.sdUpdateListener = Lists.newArrayList();
        this.objectMapper = objectMapper;
        this.serverHealth = serverHealth;

        if (StringUtils.isEmpty(thisInstance.getAddress())
                || this.thisInstance.getAddress().equalsIgnoreCase("none")
                || this.thisInstance.getAddress().equalsIgnoreCase("null")) {
            Collection<InetAddress> ips = ConfigurationHolder.getAllLocalIPs();
            if (ips.size() > 0) {
                thisInstance.setAddress(ips.iterator().next().getHostAddress());
            }
        }

        serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class).client(client).basePath(path)
                .serializer(new JsonInstanceSerializer<>(InstanceDetails.class)).thisInstance(thisInstance).build();
        initializeConfigurationChangeWatcher();
    }

    public void start() throws Exception {
        serviceDiscovery.start();
    }

    protected void initializeConfigurationChangeWatcher() {
        sdTreeCashe = new TreeCache(zkClient, basePath);
        sdTreeCashe.getListenable().addListener((client, event) -> {
            if (event.getData() != null && event.getData().getData() != null && event.getData().getData().length > 0) {
                String eventPath = event.getData().getPath();
                switch (event.getType()) {
                    case NODE_REMOVED:
                    case INITIALIZED:
                    case NODE_ADDED:
                    case NODE_UPDATED:
                        ServiceInstance<InstanceDetails> updated = objectMapper.readValue(event.getData().getData(), ServiceInstance.class);
//                        if (thisInstance.getAddress().equalsIgnoreCase(updated.getAddress())
//                                && thisInstance.getPayload().getServiceType().equals(updated.getPayload().getServiceType())) {
                        if (
                                thisInstance.getId().equalsIgnoreCase(updated.getId())
                        ) {
                            log.info("SD update for the node: " + objectMapper.writeValueAsString(thisInstance));
                            thisInstance = updated;
                            boolean maintenance = thisInstance.getPayload().isMaintenance();
                            serverHealth.getHealthIndicator().set(maintenance ? 1 : 0);
                        }

                        log.info(MessageFormat.format("SD root updated - {0}, {1}", event.getType().name(), eventPath));
                        break;
                }

                if (sdUpdateListener.size() > 0) {
                    for (ServiceDiscoveryUpdateListener listener : sdUpdateListener) {
                        listener.onUpdate(eventPath);
                    }
                }
            }
        });
        try {
            sdTreeCashe.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        CloseableUtils.closeQuietly(serviceDiscovery);
    }
}
