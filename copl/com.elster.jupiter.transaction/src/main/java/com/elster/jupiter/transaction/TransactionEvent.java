/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import org.osgi.service.event.*;

import com.elster.jupiter.util.time.StopWatch;
import com.google.common.collect.ImmutableMap;

import java.text.MessageFormat;

public final class TransactionEvent {

    private static final long NANOS_PER_MICRO = 1000L;
    private final StopWatch stopWatch;
    private final int sqlCount;
    private final int fetchCount;
    private final boolean failed;

    public TransactionEvent(boolean failed, StopWatch stopWatch, int sqlCount, int fetchCount) {
        this.failed = failed;
        this.stopWatch = stopWatch;
        this.sqlCount = sqlCount;
        this.fetchCount = fetchCount;
    }

    @Override
    public String toString() {
        String operation = failed ? "Rolled back" : "Committed";
        String base = "{0} Transaction executed in {1} \u00b5s, executed {2} statements, fetched {3} tuples";
        return MessageFormat.format(base, operation, (stopWatch.getElapsed() / NANOS_PER_MICRO), sqlCount, fetchCount);
    }

    public int getSqlCount() {
    	return sqlCount;
    }
    
    public int getFetchCount() {
        return fetchCount;
    }

    public StopWatch getStopWatch() {
        return stopWatch;
    }

    public Event toOsgiEvent() {
        ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<>();
        builder
                .put("elapsed", stopWatch.getElapsed() / NANOS_PER_MICRO)
                .put("cpu", stopWatch.getCpu() / NANOS_PER_MICRO)
                .put("statements", sqlCount)
                .put("fetchCount", fetchCount)
                .put(EventConstants.TIMESTAMP, System.currentTimeMillis());
        if (failed) {
            builder.put("failed", true);
        }
        return new Event("com/elster/jupiter/transaction/TRANSACTION", builder.build());
    }

    public boolean hasFailed() {
        return failed;
    }

}
