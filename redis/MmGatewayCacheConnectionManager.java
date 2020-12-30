package com.han.startup.redis;

import com.ubisoft.hfx.common.annotation.LimitedToMatchMakingServiceStack;
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
@LimitedToMatchMakingServiceStack
public class MmGatewayCacheConnectionManager extends RedisConnectionPool implements RedisConnectionManager {

    public static final String MM_REQUEST_EVENT_KEY_PREFIX = "mme_r_";
    public static final String MM_REQUEST_KEY_PREFIX = "mm_r_";

    @Value("${mm.gateway.redis.host.address}")
    private String redisAddress;

    @Value("${mm.gateway.redis.host.port}")
    private int port;

    @Value("${mm.gateway.redis.connection.timeout}")
    private int connectionTimeout;

    @Getter
    RedisClient redisClient;

    @Getter
    RedisClient redisPubSubClient;

    @Getter
    BoundedAsyncPool<StatefulRedisConnection<String, String>> asyncConnectionPool;
    StatefulRedisConnection<String, String> connection;
    StatefulRedisPubSubConnection<String, String> pubSubConnection;
    RedisURI redisURI;

    @PostConstruct
    void prepare() {
        redisURI = RedisURI.builder().withHost(redisAddress).withPort(port).withTimeout(Duration.of(connectionTimeout, ChronoUnit.MILLIS)).build();
        redisClient = RedisClient.create(clientResources);
        if (isConnectionPoolingEnabled()) {
            asyncConnectionPool = createdBoundedConnectionPool(redisClient, redisURI, 100, 50, 1);
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
