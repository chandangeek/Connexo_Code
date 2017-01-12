package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryType;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataValidationSummaryFlag;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ChannelDataValidationSummaryImpl implements ChannelDataValidationSummary {

    private int overallValue;
    private Map<IChannelDataValidationSummaryFlag, Integer> statistics = new HashMap<>();
    private Range<Instant> interval;
    private ChannelDataValidationSummaryType type;

    ChannelDataValidationSummaryImpl(Range<Instant> interval, ChannelDataValidationSummaryType type) {
        this.interval = interval;
        this.type = type;
    }

    void incrementFlag(IChannelDataValidationSummaryFlag flag, int increment) {
        statistics.compute(flag, (key, value) -> value == null ? increment : value + increment);
    }

    void incrementOverallValue(int increment) {
        overallValue += increment;
    }

    @Override
    public int getSum() {
        return overallValue;
    }

    @Override
    public Map<IChannelDataValidationSummaryFlag, Integer> getValues() {
        return Collections.unmodifiableMap(statistics);
    }

    @Override
    public Range<Instant> getTargetInterval() {
        return interval;
    }

    @Override
    public ChannelDataValidationSummaryType getType() {
        return type;
    }
}
