package com.han.startup.zk.discovery;

import com.ubisoft.hfx.zk.discovery.details.InstanceProvider;
import com.ubisoft.hfx.zk.discovery.details.ServiceCacheListener;
import org.apache.curator.framework.listen.Listenable;

import java.io.Closeable;
import java.util.List;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface ServiceCache<T> extends Closeable, Listenable<ServiceCacheListener>, InstanceProvider<T> {
	List<ServiceInstance<T>> getInstances();

	void start() throws Exception;
}
