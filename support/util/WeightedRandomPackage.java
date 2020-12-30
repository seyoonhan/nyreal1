package com.han.startup.support.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedRandomPackage<T> {

    private class Entry {
        double accumulatedWeight;
        T object;
    }

    private List<Entry> entries = new ArrayList<>();
    private double accumulatedWeight;
    private Random rand = new Random();
//    private ReentrantLock reentrantLock = new ReentrantLock();

    public void addEntry(T object, double weight) {
//        reentrantLock.lock();
        accumulatedWeight += weight;
        Entry e = new Entry();
        e.object = object;
        e.accumulatedWeight = accumulatedWeight;
        entries.add(e);
//        reentrantLock.unlock();
    }
//
    // this will need entire rebuild of the list
//    public void removeEntry(T object, double weight) {
//        reentrantLock.lock();
//        accumulatedWeight -= weight;
//        entries.forEach(entry -> {
//            if (entry.object.equals(object)) {
//                entry.accumulatedWeight -= weight;
//                return;
//            }
//        });
//        reentrantLock.unlock();
//    }

    public T getRandom() {
        double r = rand.nextDouble() * accumulatedWeight;

        for (Entry entry : entries) {
            if (entry.accumulatedWeight >= r) {
                return entry.object;
            }
        }

        return null;
    }
}
