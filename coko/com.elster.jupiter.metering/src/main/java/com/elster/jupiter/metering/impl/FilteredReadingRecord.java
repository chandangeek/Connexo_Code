package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredReadingRecord extends FilteredBaseReadingRecord implements ReadingRecord {
	
    FilteredReadingRecord(ReadingRecord filtered, int... indices) {
        super(filtered,indices);
    }
    
    @Override
    public String getReadingTypeCode() {
    	return getReadingType().getMRID();
    }

	@Override
	public String getReason() {
		return null;
	}

}
