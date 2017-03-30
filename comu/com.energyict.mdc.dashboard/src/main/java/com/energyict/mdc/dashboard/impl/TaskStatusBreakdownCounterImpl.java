/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * Provides an implementation for the {@link TaskStatusBreakdownCounter} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (13:15)
 */
public class TaskStatusBreakdownCounterImpl<T> implements TaskStatusBreakdownCounter<T> {

    private final T target;
    private final Map<TaskStatus, Long> counters = new EnumMap<>(TaskStatus.class);

    public TaskStatusBreakdownCounterImpl(T target) {
        super();
        this.target = target;
        this.initializeCounters();
    }

    public TaskStatusBreakdownCounterImpl(T target, long successCount, long failedCount, long pendingCount) {
        this(target);
        this.setSuccessCount(successCount);
        this.setFailedCount(failedCount);
        this.setPendingCount(pendingCount);
    }

    private void initializeCounters() {
        for (TaskStatus taskStatus : EnumSet.of(TaskStatus.Waiting, TaskStatus.Failed, TaskStatus.Pending)) {
            this.counters.put(taskStatus, 0L);
        }
    }

    @Override
    public long getSuccessCount() {
        return this.counters.get(TaskStatus.Waiting);
    }

    public void setSuccessCount(long count) {
        this.counters.put(TaskStatus.Waiting, count);
    }

    @Override
    public long getFailedCount() {
        return this.counters.get(TaskStatus.Failed);
    }

    public void setFailedCount(long count) {
        this.counters.put(TaskStatus.Failed, count);
    }

    @Override
    public long getPendingCount() {
        return this.counters.get(TaskStatus.Pending);
    }

    public void setPendingCount(long count) {
        this.counters.put(TaskStatus.Pending, count);
    }

    @Override
    public long getCount() {
        return this.getSuccessCount() + this.getFailedCount() + this.getPendingCount();
    }

    @Override
    public T getCountTarget() {
        return this.target;
    }

}