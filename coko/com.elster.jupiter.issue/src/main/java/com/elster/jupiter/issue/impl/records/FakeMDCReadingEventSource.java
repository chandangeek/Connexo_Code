package com.elster.jupiter.issue.impl.records;

//TODO delete this class when events will be sent by MDC

/**
 * This class can be used only in test purpose
 */
@Deprecated
public class FakeMDCReadingEventSource {
    private long timestamp;
    private long start;
    private long end;
    private long meterId;

    public FakeMDCReadingEventSource(long timestamp, long start, long end, long meterId) {
        this.timestamp = timestamp;
        this.start = start;
        this.end = end;
        this.meterId = meterId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getMeterId() {
        return meterId;
    }
}
