package com.han.startup.zk.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ubisoft.hfx.mm.model.MatchMakingLoadBalancingRule;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

public interface ZkNodeMatchMakingConfigurationEventListener {
    void onEvent(TreeCacheEvent event, String eventPath, MatchMakingLoadBalancingRule data) throws JsonProcessingException, InterruptedException;
}
