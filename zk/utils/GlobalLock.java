package com.han.startup.zk.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.curator.RetryLoop;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.PathAndBytesable;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.Lease;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.PathUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class GlobalLock {


	private static final String LOCK_PARENT = "locks";
	private static final String LEASE_PARENT = "leases";
	public static final Set<String> LOCK_SCHEMA = Sets.newHashSet(
			LOCK_PARENT,
			LEASE_PARENT
	);

	private static final String LEASE_BASE_NAME = "lease-";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final InterProcessMutex lock;
	private final CuratorFramework client;
	private final String leasesPath;
	private final Watcher watcher = event -> notifyFromWatcher();
	protected volatile int maxLeases;
	private volatile byte[] nodeData;

	public GlobalLock(CuratorFramework client, String path) {
		this(client, path, 1, null);
	}

	public GlobalLock(CuratorFramework client, String path, int maxLeases) {
		this(client, path, maxLeases, null);
	}

	private GlobalLock(CuratorFramework client, String path, int maxLeases, SharedCountReader count) {
		this.client = client;
		path = PathUtils.validatePath(path);
		lock = new InterProcessMutex(client, ZKPaths.makePath(path, LOCK_PARENT));
		this.maxLeases = (count != null) ? count.getCount() : maxLeases;
		leasesPath = ZKPaths.makePath(path, LEASE_PARENT);

		if (count != null) {
			count.addListener
					(
							new SharedCountListener() {
								@Override
								public void countHasChanged(SharedCountReader sharedCount, int newCount) throws Exception {
									GlobalLock.this.maxLeases = newCount;
									notifyFromWatcher();
								}

								@Override
								public void stateChanged(CuratorFramework client, ConnectionState newState) {
								}
							}
					);
		}
	}

	private synchronized void notifyFromWatcher() {
		notifyAll();
	}

	public GlobalLock(CuratorFramework client, String path, SharedCountReader count) {
		this(client, path, 0, count);
	}

	public Collection<Lease> acquire(int qty) throws Exception {
		return acquire(qty, 0, null);
	}

	public Collection<Lease> acquire(int qty, long time, TimeUnit unit) throws Exception {
		long startMs = System.currentTimeMillis();
		boolean hasWait = (unit != null);
		long waitMs = hasWait ? TimeUnit.MILLISECONDS.convert(time, unit) : 0;

		Preconditions.checkArgument(qty > 0, "qty cannot be 0");

		ImmutableList.Builder<Lease> builder = ImmutableList.builder();
		boolean success = false;
		try {
			while (qty-- > 0) {
				int retryCount = 0;
				long startMillis = System.currentTimeMillis();
				boolean isDone = false;
				while (!isDone) {
					switch (internalAcquire1Lease(builder, startMs, hasWait, waitMs)) {
						case CONTINUE: {
							isDone = true;
							break;
						}

						case RETURN_NULL: {
							return null;
						}

						case RETRY_DUE_TO_MISSING_NODE: {
							if (!client.getZookeeperClient().getRetryPolicy().allowRetry(retryCount++, System.currentTimeMillis() - startMillis, RetryLoop.getDefaultRetrySleeper())) {
								throw new KeeperException.NoNodeException("Sequential path not found - possible session loss");
							}
							break;
						}
					}
				}
			}
			success = true;
		} finally {
			if (!success) {
				returnAll(builder.build());
			}
		}

		return builder.build();
	}

	private InternalAcquireResult internalAcquire1Lease(ImmutableList.Builder<Lease> builder, long startMs, boolean hasWait, long waitMs) throws Exception {
		if (client.getState() != CuratorFrameworkState.STARTED) {
			return InternalAcquireResult.RETURN_NULL;
		}

		if (hasWait) {
			long thisWaitMs = getThisWaitMs(startMs, waitMs);
			if (!lock.acquire(thisWaitMs, TimeUnit.MILLISECONDS)) {
				return InternalAcquireResult.RETURN_NULL;
			}
		} else {
			lock.acquire();
		}
		try {
			PathAndBytesable<String> createBuilder = client.create().creatingParentContainersIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL);
			String path = (nodeData != null) ? createBuilder.forPath(ZKPaths.makePath(leasesPath, LEASE_BASE_NAME), nodeData) : createBuilder.forPath(ZKPaths.makePath(leasesPath, LEASE_BASE_NAME));
			String nodeName = ZKPaths.getNodeFromPath(path);
			builder.add(makeLease(path));

			synchronized (this) {
				for (; ; ) {
					List<String> children = client.getChildren().usingWatcher(watcher).forPath(leasesPath);
					if (!children.contains(nodeName)) {
						log.error("Sequential path not found: " + path);
						return InternalAcquireResult.RETRY_DUE_TO_MISSING_NODE;
					}

					if (children.size() <= maxLeases) {
						break;
					}
					if (hasWait) {
						long thisWaitMs = getThisWaitMs(startMs, waitMs);
						if (thisWaitMs <= 0) {
							return InternalAcquireResult.RETURN_NULL;
						}
						wait(thisWaitMs);
					} else {
						wait();
					}
				}
			}
		} finally {
			lock.release();
		}
		return InternalAcquireResult.CONTINUE;
	}

	public void returnAll(Collection<Lease> leases) {
		for (Lease l : leases) {
			CloseableUtils.closeQuietly(l);
		}
	}

	private long getThisWaitMs(long startMs, long waitMs) {
		long elapsedMs = System.currentTimeMillis() - startMs;
		return waitMs - elapsedMs;
	}

	private Lease makeLease(final String path) {
		return new Lease() {
			@Override
			public void close() throws IOException {
				try {
					client.delete().guaranteed().forPath(path);
				} catch (KeeperException.NoNodeException e) {
					log.warn("Lease already released:" + e.getMessage());
				} catch (Exception e) {
					throw new IOException(e);
				}
			}

			@Override
			public byte[] getData() throws Exception {
				return client.getData().forPath(path);
			}

			@Override
			public String getNodeName() {
				//TODO: implement
				return null;
			}
		};
	}

	public Lease acquire(long time, TimeUnit unit) throws Exception {
		Collection<Lease> leases = acquire(1, time, unit);
		return (leases != null) ? leases.iterator().next() : null;
	}

	public void setNodeData(byte[] nodeData) {
		this.nodeData = (nodeData != null) ? Arrays.copyOf(nodeData, nodeData.length) : null;
	}

	public Collection<String> getParticipantNodes() throws Exception {
		return client.getChildren().forPath(leasesPath);
	}

	public void returnLease(Lease lease) {
		org.apache.curator.utils.CloseableUtils.closeQuietly(lease);
	}

	public Lease acquire() throws Exception {
		Collection<Lease> leases = acquire(1, 0, null);
		return leases.iterator().next();
	}

	private enum InternalAcquireResult {
		CONTINUE,
		RETURN_NULL,
		RETRY_DUE_TO_MISSING_NODE
	}

}

