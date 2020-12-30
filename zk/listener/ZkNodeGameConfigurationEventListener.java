package com.han.startup.zk.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ubisoft.hfx.common.GameConfigurationData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

public interface ZkNodeGameConfigurationEventListener {
    void onEvent(TreeCacheEvent event, String eventPath, GameConfigurationData configurationData) throws JsonProcessingException, InterruptedException;
}
