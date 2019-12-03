package com.energyict.mdc.engine.impl.core.remote;

public class GenericWrapper<K, V> {

    private K first;
    private V last;

    public K getFirst() {
        return first;
    }

    public V getLast() {
        return last;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    @Override
    public boolean equals(Object obj) {
        return first.equals(((GenericWrapper)obj).getFirst()) && last.equals(((GenericWrapper)obj).getLast());
    }

    @Override
    public int hashCode() {
        return 31 * first.hashCode() + last.hashCode();
    }
}
