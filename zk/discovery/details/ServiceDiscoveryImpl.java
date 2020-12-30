package com.han.startup.zk.discovery.details;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ubisoft.hfx.zk.discovery.*;
import com.ubisoft.hfx.zk.discovery.strategies.RoundRobinStrategy;
import com.ubisoft.hfx.zk.utils.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
public class ServiceDiscoveryImpl<T> implements ServiceDiscovery<T> {
	private final CuratorFramework client;
	private final String basePath;
	private final InstanceSerializer<T> serializer;
	private final ConcurrentMap<String, Entry<T>> services = Maps.newConcurrentMap();
	private final Collection<ServiceCache<T>> caches = Sets.newSetFromMap(Maps.<ServiceCache<T>, Boolean>newConcurrentMap());
	private final Collection<ServiceProvider<T>> providers = Sets.newSetFromMap(Maps.<ServiceProvider<T>, Boolean>newConcurrentMap());
	private final boolean watchInstances;
	private final ConnectionStateListener connectionStateListener = (client, newState) -> {
		if ((newState == ConnectionState.RECONNECTED) || (newState == ConnectionState.CONNECTED)) {
			try {
				log.debug("Re-registering due to reconnection");
				reRegisterServices();
			} catch (Exception e) {
				ThreadUtils.checkInterrupted(e);
				log.error("Could not re-register instances after reconnection", e);
			}
		}
	};

	public ServiceDiscoveryImpl(CuratorFramework client, String basePath, InstanceSerializer<T> serializer, ServiceInstance<T> thisInstance, boolean watchInstances) {
		this.watchInstances = watchInstances;
		this.client = Preconditions.checkNotNull(client, "client cannot be null");
		this.basePath = Preconditions.checkNotNull(basePath, "basePath cannot be null");
		this.serializer = Preconditions.checkNotNull(serializer, "serializer cannot be null");
		if (thisInstance != null) {
			Entry<T> entry = new Entry<T>(thisInstance);
			entry.cache = makeNodeCache(thisInstance);
			services.put(thisInstance.getId(), entry);
		}
	}

	private NodeCache makeNodeCache(final ServiceInstance<T> instance) {
		if (!watchInstances) {
			return null;
		}

		final NodeCache nodeCache = new NodeCache(client, pathForInstance(instance.getName(), instance.getId()));
		try {
			nodeCache.start(true);
		} catch (Exception e) {
			ThreadUtils.checkInterrupted(e);
			log.error("Could not start node cache for: " + instance, e);
		}
		NodeCacheListener listener = () -> {
			if (nodeCache.getCurrentData() != null) {
				ServiceInstance<T> newInstance = serializer.deserialize(nodeCache.getCurrentData().getData());
				Entry<T> entry = services.get(newInstance.getId());
				if (entry != null) {
					synchronized (entry) {
						entry.service = newInstance;
					}
				}
			} else {
				log.warn("Instance data has been deleted for: " + instance);
			}
		};
		nodeCache.getListenable().addListener(listener);
		return nodeCache;
	}

	@VisibleForTesting
    String pathForInstance(String name, String id) {
		return ZKPaths.makePath(pathForName(name), id);
	}

	String pathForName(String name) {
		return ZKPaths.makePath(basePath, name);
	}

	@Override
	public void start() throws Exception {
		try {
			reRegisterServices();
		} catch (KeeperException e) {
			log.error("Could not register instances - will try again later", e);
		}
		client.getConnectionStateListenable().addListener(connectionStateListener);
	}

	@Override
	public void registerService(ServiceInstance<T> service) throws Exception {
		Entry<T> newEntry = new Entry<T>(service);
		Entry<T> oldEntry = services.putIfAbsent(service.getId(), newEntry);
		Entry<T> useEntry = (oldEntry != null) ? oldEntry : newEntry;
		synchronized (useEntry) {
			if (useEntry == newEntry) {
				useEntry.cache = makeNodeCache(service);
			}
			internalRegisterService(service);
		}
	}

	@Override
	public void updateService(final ServiceInstance<T> service) throws Exception {
		Entry<T> entry = services.get(service.getId());
		if (entry == null) {
			throw new Exception("Service not registered: " + service);
		}
		synchronized (entry) {
			entry.service = service;
			byte[] bytes = serializer.serialize(service);
			String path = pathForInstance(service.getName(), service.getId());
			client.setData().forPath(path, bytes);
		}
	}

	@Override
	public void unregisterService(ServiceInstance<T> service) throws Exception {
		Entry<T> entry = services.remove(service.getId());
		internalUnregisterService(entry);
	}

	@Override
	public ServiceCacheBuilder<T> serviceCacheBuilder() {
		return new ServiceCacheBuilderImpl<T>(this)
				.threadFactory(ThreadUtils.newThreadFactory("ServiceCache"));
	}

	@Override
	public Collection<String> queryForNames() throws Exception {
		List<String> names = client.getChildren().forPath(basePath);
		return ImmutableList.copyOf(names);
	}

	@Override
	public Collection<ServiceInstance<T>> queryForInstances(String name) throws Exception {
		return queryForInstances(name, null);
	}

	@Override
	public ServiceInstance<T> queryForInstance(String name, String id) throws Exception {
		String path = pathForInstance(name, id);
		try {
			byte[] bytes = client.getData().forPath(path);
			return serializer.deserialize(bytes);
		} catch (KeeperException.NoNodeException ignore) {
			// ignore
		}
		return null;
	}

	@Override
	public ServiceProviderBuilder<T> serviceProviderBuilder() {
		return new ServiceProviderBuilderImpl<>(this)
				.providerStrategy(new RoundRobinStrategy<>())
				.threadFactory(ThreadUtils.newThreadFactory("ServiceProvider"));
	}

	private void internalUnregisterService(final Entry<T> entry) throws Exception {
		if (entry != null) {
			synchronized (entry) {
				if (entry.cache != null) {
					CloseableUtils.closeQuietly(entry.cache);
					entry.cache = null;
				}

				String path = pathForInstance(entry.service.getName(), entry.service.getId());
				try {
					client.delete().guaranteed().forPath(path);
				} catch (KeeperException.NoNodeException ignore) {
					// ignore
				}
			}
		}
	}

	private void reRegisterServices() throws Exception {
		for (final Entry<T> entry : services.values()) {
			synchronized (entry) {
				internalRegisterService(entry.service);
			}
		}
	}

	@VisibleForTesting
	protected void internalRegisterService(ServiceInstance<T> service) throws Exception {
		byte[] bytes = serializer.serialize(service);
		String path = pathForInstance(service.getName(), service.getId());

		final int MAX_TRIES = 2;
		boolean isDone = false;
		for (int i = 0; !isDone && (i < MAX_TRIES); ++i) {
			try {
				CreateMode mode;
				switch (service.getServiceType()) {
					case DYNAMIC:
						mode = CreateMode.EPHEMERAL;
						break;
					case DYNAMIC_SEQUENTIAL:
						mode = CreateMode.EPHEMERAL_SEQUENTIAL;
						break;
					default:
						mode = CreateMode.PERSISTENT;
						break;
				}
				client.create().creatingParentContainersIfNeeded().withMode(mode).forPath(path, bytes);
				isDone = true;
			} catch (KeeperException.NodeExistsException e) {
				client.delete().forPath(path);
			}
		}
	}

	@Override
	public void close() throws IOException {
		for (ServiceCache<T> cache : Lists.newArrayList(caches)) {
			CloseableUtils.closeQuietly(cache);
		}
		for (ServiceProvider<T> provider : Lists.newArrayList(providers)) {
			CloseableUtils.closeQuietly(provider);
		}

		for (Entry<T> entry : services.values()) {
			try {
				internalUnregisterService(entry);
			} catch (KeeperException.NoNodeException ignore) {
				// ignore
			} catch (Exception e) {
				ThreadUtils.checkInterrupted(e);
				log.error("Could not unregister instance: " + entry.service.getName(), e);
			}
		}

		client.getConnectionStateListenable().removeListener(connectionStateListener);
	}

	void cacheOpened(ServiceCache<T> cache) {
		caches.add(cache);
	}

	void cacheClosed(ServiceCache<T> cache) {
		caches.remove(cache);
	}

	void providerOpened(ServiceProvider<T> provider) {
		providers.add(provider);
	}

	void providerClosed(ServiceProvider<T> cache) {
		providers.remove(cache);
	}

	CuratorFramework getClient() {
		return client;
	}

	InstanceSerializer<T> getSerializer() {
		return serializer;
	}

	List<ServiceInstance<T>> queryForInstances(String name, Watcher watcher) throws Exception {
		ImmutableList.Builder<ServiceInstance<T>> builder = ImmutableList.builder();
		String path = pathForName(name);
		List<String> instanceIds;

		if (watcher != null) {
			instanceIds = getChildrenWatched(path, watcher, true);
		} else {
			try {
				instanceIds = client.getChildren().forPath(path);
			} catch (KeeperException.NoNodeException e) {
				instanceIds = Lists.newArrayList();
			}
		}

		for (String id : instanceIds) {
			ServiceInstance<T> instance = queryForInstance(name, id);
			if (instance != null) {
				builder.add(instance);
			}
		}
		return builder.build();
	}

	@VisibleForTesting
	int debugServicesQty() {
		return services.size();
	}

	private List<String> getChildrenWatched(String path, Watcher watcher, boolean recurse) throws Exception {
		List<String> instanceIds;
		try {
			instanceIds = client.getChildren().usingWatcher(watcher).forPath(path);
		} catch (KeeperException.NoNodeException e) {
			if (recurse) {
				try {
					client.create().creatingParentContainersIfNeeded().forPath(path);
				} catch (KeeperException.NodeExistsException ignore) {
					// ignore
				}
				instanceIds = getChildrenWatched(path, watcher, false);
			} else {
				throw e;
			}
		}
		return instanceIds;
	}

	@VisibleForTesting
	ServiceInstance<T> getRegisteredService(String id) {
		Entry<T> entry = services.get(id);
		return (entry != null) ? entry.service : null;
	}

	private static class Entry<T> {
		private volatile ServiceInstance<T> service;
		private volatile NodeCache cache;

		private Entry(ServiceInstance<T> service) {
			this.service = service;
		}
	}
}
