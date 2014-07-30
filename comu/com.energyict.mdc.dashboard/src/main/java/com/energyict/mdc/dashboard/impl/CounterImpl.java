package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.Counter;

/**
 * Provides an implementation for the {@link Counter} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (10:36)
 */
public class CounterImpl<T> implements Counter<T> {

    private final T target;
    private long count;

    public CounterImpl(T target) {
        this(target, 0);
    }

    public CounterImpl(T target, long count) {
        super();
        this.target = target;
        this.count = count;
    }

    @Override
    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void increment () {
        this.count++;
    }

    public void add (long value) {
        this.count = count + value;
    }

    @Override
    public T getCountTarget() {
        return this.target;
    }

}