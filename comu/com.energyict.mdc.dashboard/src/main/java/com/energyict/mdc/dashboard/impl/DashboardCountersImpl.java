/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardCounters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link DashboardCounters} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (10:40)
 */
abstract class DashboardCountersImpl<T> implements DashboardCounters<T> {

    private List<Counter<T>> counters = new ArrayList<>();

    DashboardCountersImpl() {
        super();
    }

    DashboardCountersImpl(Counter<T>... counters) {
        this();
        for (Counter<T> counter : counters) {
            this.add(counter);
        }
    }

    public void add(Counter<T> counter) {
        this.counters.add(counter);
    }

    @Override
    public Iterator<Counter<T>> iterator() {
        return Collections.unmodifiableList(this.counters).iterator();
    }

    @Override
    public long getTotalCount() {
        long count = 0;
        for (Counter<T> counter : this.counters) {
            count = count + counter.getCount();
        }
        return count;
    }

}