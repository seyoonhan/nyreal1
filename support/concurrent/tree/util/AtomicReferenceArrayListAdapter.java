package com.han.startup.support.concurrent.tree.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicReferenceArrayListAdapter<T> extends AbstractList<T> implements Serializable {

    private final AtomicReferenceArray<T> atomicReferenceArray;

    public AtomicReferenceArrayListAdapter(AtomicReferenceArray<T> atomicReferenceArray) {
        this.atomicReferenceArray = atomicReferenceArray;
    }

    @Override
    public T get(int index) {
        return atomicReferenceArray.get(index);
    }

    @Override
    public int size() {
        return atomicReferenceArray.length();
    }
}
