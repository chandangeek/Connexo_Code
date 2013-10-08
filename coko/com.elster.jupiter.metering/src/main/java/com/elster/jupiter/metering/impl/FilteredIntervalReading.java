package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.IntervalReading;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.collections.KPermutation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
