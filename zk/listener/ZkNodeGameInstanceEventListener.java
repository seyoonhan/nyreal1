package com.han.startup.zk.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ubisoft.hfx.mm.model.GameInstance;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

public interface ZkNodeGameInstanceEventListener {
    void onEvent(TreeCacheEvent event, String eventPath, GameInstance gameInstance) throws JsonProcessingException, InterruptedException;
}
