package com.han.startup.redis;

//import redis.clients.jedis.JedisPool;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

public interface RedisConnectionManager {
    int GENERAL_OPERATION_TIMEOUT = 1000;
//    JedisPool getJedisPool();

    boolean ENABLE_CONNECTION_POOLING = false;

    boolean isConnectionPoolingEnabled();

    StatefulRedisConnection<String, String> getConnection();

    StatefulRedisPubSubConnection<String, String> getPubSubConnection();
}
