/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.ChannelDataCompletionSummaryType;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataCompletionSummary;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataCompletionSummaryFlag;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ChannelDataCompletionSummaryImpl implements IChannelDataCompletionSummary {

    private int overallValue;
    private Map<IChannelDataCompletionSummaryFlag, Integer> statistics = new HashMap<>();
    private Range<Instant> interval;
    private ChannelDataCompletionSummaryType type;

    ChannelDataCompletionSummaryImpl(Range<Instant> interval, ChannelDataCompletionSummaryType type) {
        this.interval = interval;
        this.type = type;
    }

    void incrementFlag(IChannelDataCompletionSummaryFlag flag, int increment) {
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
    public Map<IChannelDataCompletionSummaryFlag, Integer> getValues() {
        return Collections.unmodifiableMap(statistics);
    }

    @Override
    public Range<Instant> getTargetInterval() {
        return interval;
    }

    @Override
    public ChannelDataCompletionSummaryType getType() {
        return type;
    }
}
