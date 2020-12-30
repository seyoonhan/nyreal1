package com.han.startup.zk.discovery.details;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.ubisoft.hfx.zk.discovery.InstanceFilter;
import com.ubisoft.hfx.zk.discovery.ServiceInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by shan2 on 7/18/2017.
 */
@Slf4j
class FilteredInstanceProvider<T> implements InstanceProvider<T> {
	private final InstanceProvider<T> instanceProvider;
	private final Predicate<ServiceInstance<T>> predicates;

	FilteredInstanceProvider(InstanceProvider<T> instanceProvider, List<InstanceFilter<T>> filters) {
		this.instanceProvider = instanceProvider;
		predicates = Predicates.and(filters);
	}

	@Override
	public List<ServiceInstance<T>> getInstances() throws Exception {
		Iterable<ServiceInstance<T>> filtered = Iterables.filter(instanceProvider.getInstances(), predicates);
		return ImmutableList.copyOf(filtered);
	}
}
