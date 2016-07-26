package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.time.Instant;
import java.util.List;

public class ChannelDataValidationSummaryInfo {
    public long id;
    public String name;
    public Instant intervalStart;
    public Instant intervalEnd;
    public int total;
    public List<ChannelDataValidationSummaryFlagInfo> statistics;

    ChannelDataValidationSummaryInfo(long id, String name, Instant intervalStart, Instant intervalEnd, int total,
                                     List<ChannelDataValidationSummaryFlagInfo> statistics) {
        this.id = id;
        this.name = name;
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
        this.total = total;
        this.statistics = statistics;
    }
}
