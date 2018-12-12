/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredIntervalReadingRecord extends FilteredBaseReadingRecord implements IntervalReadingRecord {

    private final IntervalReadingRecord source;    

    FilteredIntervalReadingRecord(IntervalReadingRecordImpl source, int... indices) {
        super(source,indices);
        this.source = source;        
    }

    @Override
    public IntervalReadingRecord filter(ReadingType readingType) {
    	return source.filter(readingType);
    }
}
