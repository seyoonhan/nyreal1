package com.han.startup.zk.discovery.details;

import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface ServiceCacheListener extends ConnectionStateListener {
	void cacheChanged();
}
