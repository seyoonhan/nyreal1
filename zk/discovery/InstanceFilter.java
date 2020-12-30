package com.han.startup.zk.discovery;

import com.google.common.base.Predicate;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface InstanceFilter<T> extends Predicate<ServiceInstance<T>> {
}
