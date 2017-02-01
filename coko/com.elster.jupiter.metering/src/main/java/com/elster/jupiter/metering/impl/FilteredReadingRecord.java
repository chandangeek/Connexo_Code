/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredReadingRecord extends FilteredBaseReadingRecord implements ReadingRecord {
	
	private final ReadingRecordImpl filtered;
	
    FilteredReadingRecord(ReadingRecordImpl filtered, int... indices) {
        super(filtered,indices);
        this.filtered = filtered;
    }
    
    @Override
    public String getReadingTypeCode() {
    	return getReadingType().getMRID();
    }

	@Override
	public String getReason() {
		return null;
	}
	
	@Override
	public String getText() {
		return filtered.getText();
	}
	
	 @Override
	public ReadingRecord filter(ReadingType readingType) {
	    return filtered.filter(readingType);
	}

}
