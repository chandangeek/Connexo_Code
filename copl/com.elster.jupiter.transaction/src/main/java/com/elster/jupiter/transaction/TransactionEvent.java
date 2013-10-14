package com.elster.jupiter.transaction;

import org.osgi.service.event.*;

import com.elster.jupiter.util.time.StopWatch;
import com.google.common.collect.ImmutableMap;

import java.text.MessageFormat;

public final class TransactionEvent {

    private static final long NANOS_PER_MICRO = 1000L;
    private final Transaction<?> transaction;
    private final StopWatch stopWatch;
    private final int sqlCount;
    private final int fetchCount;
    private final boolean failed;

    public TransactionEvent(Transaction<?> transaction, boolean failed, StopWatch stopWatch, int sqlCount, int fetchCount) {
        this.transaction = transaction;
        this.failed = failed;
        this.stopWatch = stopWatch;
        this.sqlCount = sqlCount;
        this.fetchCount = fetchCount;
    }

    @Override
    public String toString() {
        String operation = failed ? "Rolled back" : "Committed";
        String base = "{0} Transaction {1} executed in {2} \u00b5s, executed {3} statements, fetched {4} tuples";
        return MessageFormat.format(base, operation, transaction.getClass().getName(), (stopWatch.getElapsed() / NANOS_PER_MICRO), sqlCount, fetchCount);
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
                .put("transaction", transaction.getClass().getName())
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
