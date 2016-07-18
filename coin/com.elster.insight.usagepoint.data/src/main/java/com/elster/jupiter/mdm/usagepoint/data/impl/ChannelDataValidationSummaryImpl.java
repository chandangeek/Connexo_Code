package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryFlag;

import java.util.EnumMap;
import java.util.Map;

public class ChannelDataValidationSummaryImpl implements ChannelDataValidationSummary {

    private int overallValue;
    private Map<ChannelDataValidationSummaryFlag, Integer> statistics = new EnumMap<>(ChannelDataValidationSummaryFlag.class);

    void incrementFlag(ChannelDataValidationSummaryFlag flag, int increment) {
        Integer previous = statistics.get(flag);
        if (previous == null) {
            previous = 0;
        }
        statistics.put(flag, previous + increment);
    }

    void incrementOverallValue(int increment) {
        overallValue += increment;
    }

    @Override
    public int getSum() {
        return overallValue;
    }

    @Override
    public Map<ChannelDataValidationSummaryFlag, Integer> getValues() {
        return statistics;
    }
}
