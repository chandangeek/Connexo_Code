/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.ExecutionStatistics;

/**
 * Provides an implementation for the {@link ExecutionStatistics} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-10 (11:49)
 */
public class ExecutionStatisticsImpl implements ExecutionStatistics, ExecutionStatisticsImplMBean {

    private long completeCount ;
    private long timeoutCount;
    private long totalNanos;
    private long minimumNanos;
    private long maximumNanos;

    public ExecutionStatisticsImpl() {
        this.reset();
    }

    @Override
    public synchronized long getCompleteCount() {
        return this.completeCount;
    }

    @Override
    public long getTimeoutCount() {
        return this.timeoutCount;
    }

    @Override
    public synchronized long getTotalExecutionTime() {
        return this.totalNanos;
    }

    @Override
    public synchronized long getMinimumExecutionTime() {
        return this.minimumNanos;
    }

    @Override
    public synchronized long getMaximumExecutionTime() {
        return this.maximumNanos;
    }

    @Override
    public synchronized long getAverageExecutionTime() {
        if (this.completeCount > 0) {
            return this.totalNanos / this.completeCount;
        } else {
            return this.totalNanos;
        }
    }

    synchronized void reset() {
        this.completeCount = 0;
        this.timeoutCount = 0;
        this.totalNanos = 0;
        this.minimumNanos = Long.MAX_VALUE;
        this.maximumNanos = Long.MIN_VALUE;
    }

    synchronized void registerExecution(long nanos) {
        this.completeCount++;
        this.totalNanos = this.totalNanos + nanos;
        this.minimumNanos = Long.min(this.minimumNanos, nanos);
        this.maximumNanos = Long.max(this.maximumNanos, nanos);
    }

    synchronized void registerTimeout() {
        this.timeoutCount++;
    }

}