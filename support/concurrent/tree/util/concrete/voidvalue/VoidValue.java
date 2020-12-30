package com.han.startup.support.concurrent.tree.util.concrete.voidvalue;

public class VoidValue {

    public static final VoidValue SINGLETON = new VoidValue();

    VoidValue() {
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoidValue;
    }

    @Override
    public String toString() {
        return "-";
    }
}
