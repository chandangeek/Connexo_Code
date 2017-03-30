/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link TaskStatusBreakdownCounters} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (12:02)
 */
abstract class TaskStatusBreakdownCountersImpl<T> implements TaskStatusBreakdownCounters<T> {

    private List<TaskStatusBreakdownCounter<T>> counters = new ArrayList<>();

    TaskStatusBreakdownCountersImpl() {
        super();
    }

    TaskStatusBreakdownCountersImpl(TaskStatusBreakdownCounter<T>... counters) {
        this();
        for (TaskStatusBreakdownCounter<T> counter : counters) {
            this.add(counter);
        }
    }

    public void add(TaskStatusBreakdownCounter<T> counter) {
        this.counters.add(counter);
    }

    @Override
    public Iterator<TaskStatusBreakdownCounter<T>> iterator() {
        return Collections.unmodifiableList(this.counters).iterator();
    }

    @Override
    public long getTotalSuccessCount() {
        long count = 0;
        for (TaskStatusBreakdownCounter<T> counter : this.counters) {
            count = count + counter.getSuccessCount();
        }
        return count;
    }

    @Override
    public long getTotalFailedCount() {
        long count = 0;
        for (TaskStatusBreakdownCounter<T> counter : this.counters) {
            count = count + counter.getFailedCount();
        }
        return count;
    }

    @Override
    public long getTotalPendingCount() {
        long count = 0;
        for (TaskStatusBreakdownCounter<T> counter : this.counters) {
            count = count + counter.getPendingCount();
        }
        return count;
    }

    @Override
    public long getTotalCount() {
        return this.getTotalSuccessCount() + this.getTotalFailedCount() + getTotalPendingCount();
    }

}