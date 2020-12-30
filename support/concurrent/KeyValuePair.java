package com.han.startup.support.concurrent;

public interface KeyValuePair<T> {

    CharSequence getKey();

    T getValue();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}