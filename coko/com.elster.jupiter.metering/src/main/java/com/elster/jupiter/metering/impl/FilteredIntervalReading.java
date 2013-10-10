package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.IntervalReading;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredIntervalReading extends FilteredBaseReading implements IntervalReading {

    private final IntervalReading source;    

    FilteredIntervalReading(IntervalReading source, int... indices) {
        super(source,indices);
        this.source = source;        
    }

    @Override
    public long getProfileStatus() {
        return source.getProfileStatus();
    }
}
