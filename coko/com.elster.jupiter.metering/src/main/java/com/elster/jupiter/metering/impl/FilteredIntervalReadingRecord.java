package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredIntervalReadingRecord extends FilteredBaseReadingRecord implements IntervalReadingRecord {

    private final IntervalReadingRecord source;    

    FilteredIntervalReadingRecord(IntervalReadingRecord source, int... indices) {
        super(source,indices);
        this.source = source;        
    }

    @Override
    public long getProfileStatus() {
        return source.getProfileStatus();
    }
}
