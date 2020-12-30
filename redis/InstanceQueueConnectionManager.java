package com.han.startup.redis;

import com.ubisoft.hfx.common.annotation.LimitedToOrchestrationServiceStack;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.support.BoundedAsyncPool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("Duplicates")
@Component
@Slf4j
@LimitedToOrchestrationServiceStack
public class InstanceQueueConnectionManager extends RedisConnectionPool implements RedisConnectionManager {

    @Value("${instance.queue.redis.host.address}")
    private String redisAddress;

    @Value("${instance.queue.redis.host.port}")
    private int port;

    @Value("${instance.queue.redis.connection.timeout}")
    private int connectionTimeout;
    @Getter
    RedisClient redisPubSubClient;

    @Getter
    RedisClient redisClient;
    @Getter
    BoundedAsyncPool<StatefulRedisConnection<String, String>> asyncConnectionPool;
    StatefulRedisConnection<String, String> connection;
    StatefulRedisPubSubConnection<String, String> pubSubConnection;
    RedisURI redisURI;
    public static final String REMOVED_INSTANCE_KEY_PREFIX = "rmi_";

    @PostConstruct
    void prepare() {
        redisURI = RedisURI.builder().withHost(redisAddress).withPort(port).withTimeout(Duration.of(connectionTimeout, ChronoUnit.MILLIS)).build();
        redisClient = RedisClient.create(clientResources);
        if (isConnectionPoolingEnabled()) {
            asyncConnectionPool = createdBoundedConnectionPool(redisClient, redisURI, 100, 5, 1);
        } else {
            connection = redisClient.connect(StringCodec.UTF8, redisURI);
        }

        redisPubSubClient = RedisClient.create(clientResources);
        pubSubConnection = redisPubSubClient.connectPubSub(StringCodec.UTF8, redisURI);
    }

    @PreDestroy
    void shutdown() {
        if (isConnectionPoolingEnabled()) {
            asyncConnectionPool.closeAsync();
        } else {
            connection.closeAsync();
        }

        redisClient.shutdownAsync();
        pubSubConnection.closeAsync();
        redisPubSubClient.shutdownAsync();
    }

    @Override
    public boolean isConnectionPoolingEnabled() {
        return ENABLE_CONNECTION_POOLING;
    }

    @Override
    public StatefulRedisConnection<String, String> getConnection() {
        if (this.connection == null) {
            this.connection = redisClient.connect(StringCodec.UTF8, redisURI);
        }

        return this.connection;
    }

    @Override
    public StatefulRedisPubSubConnection<String, String> getPubSubConnection() {
        if (this.pubSubConnection == null) {
            this.pubSubConnection = redisClient.connectPubSub(StringCodec.UTF8, redisURI);
        }

        return this.pubSubConnection;
    }
}
