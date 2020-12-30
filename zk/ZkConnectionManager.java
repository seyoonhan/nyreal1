package com.han.startup.zk;

import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ZkConnectionManager {

    @Getter
    private CuratorFramework zkClient;

    @Value("${ZOO_CONNECTION_STRING}")
    private String zkConnectionString;

    @PostConstruct
    void prepare() {
        ExponentialBackoffRetry exponentialBackoffRetry = new ExponentialBackoffRetry(1000, 10);
        zkClient = CuratorFrameworkFactory.newClient(zkConnectionString, exponentialBackoffRetry);
        zkClient.start();
    }

}
