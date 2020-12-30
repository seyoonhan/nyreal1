package com.han.startup.support.concurrent;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class LazyIterator<T> implements Iterator<T> {

    T next = null;
    State state = State.NOT_READY;

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Iterator.remove() is not supported");
    }

    @Override
    public final boolean hasNext() {
        if (state == State.FAILED) {
            throw new IllegalStateException("This iterator is in an inconsistent state, and can no longer be used, " +
                    "due to an exception previously thrown by the computeNext() method");
        }
        switch (state) {
            case DONE:
                return false;
            case READY:
                return true;
        }
        return tryToComputeNext();
    }

    boolean tryToComputeNext() {
        state = State.FAILED; // temporary pessimism
        next = computeNext();
        if (state != State.DONE) {
            state = State.READY;
            return true;
        }
        return false;
    }

    @Override
    public final T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        state = State.NOT_READY;
        return next;
    }

    protected final T endOfData() {
        state = State.DONE;
        return null;
    }

    protected abstract T computeNext();

    enum State {READY, NOT_READY, DONE, FAILED}
}
