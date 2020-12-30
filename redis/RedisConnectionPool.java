package com.han.startup.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.support.AsyncConnectionPoolSupport;
import io.lettuce.core.support.BoundedAsyncPool;
import io.lettuce.core.support.BoundedPoolConfig;

public class RedisConnectionPool {

    protected static BoundedAsyncPool<StatefulRedisConnection<String, String>> createdBoundedConnectionPool(RedisClient redisClient, RedisURI redisURI, int maxTotal, int maxIdle, int minIdle) {
        return AsyncConnectionPoolSupport.createBoundedObjectPool(() -> redisClient.connectAsync(StringCodec.UTF8, redisURI),
                BoundedPoolConfig.builder()
                        .maxTotal(maxTotal).maxIdle(maxIdle).minIdle(minIdle).build());
    }

    protected ClientResources clientResources = DefaultClientResources.create();

}
