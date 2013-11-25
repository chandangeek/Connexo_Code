package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.util.time.Interval;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredBaseReadingRecord implements BaseReadingRecord   {

    private final BaseReadingRecord filtered;
    private final KPermutation view;

    FilteredBaseReadingRecord(BaseReadingRecord filtered, int... indices) {
        this.filtered = filtered;
        view = new KPermutation(indices);
    }

    @Override
    public long getProcessingFlags() {
        return filtered.getProcessingFlags();
    }

    @Override
    public ReadingType getReadingType() {
        return filtered.getReadingType();
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return view.perform(filtered.getReadingTypes()).get(offset);
    }

    @Override
    public List<ReadingType> getReadingTypes() {
        return view.perform(filtered.getReadingTypes());
    }

    @Override
    public Date getReportedDateTime() {
        return filtered.getReportedDateTime();
    }

    @Override
    public Date getTimeStamp() {
        return filtered.getTimeStamp();
    }

    @Override
    public BigDecimal getValue() {
        return filtered.getValue();
    }

    @Override
    public BigDecimal getValue(int offset) {
        return view.perform(filtered.getValues()).get(offset);
    }

    @Override
    public BigDecimal getValue(ReadingType readingType) {
        return filtered.getValue(readingType);
    }

    @Override
    public List<BigDecimal> getValues() {
        return view.perform(filtered.getValues());
    }

	@Override
	public BigDecimal getSensorAccuracy() {
		return null;
	}

	@Override
	public String getSource() {
		return null;
	}

	@Override
	public Interval getTimePeriod() {
		return null;
	}
   
	BaseReadingRecord getFiltered() {
		return filtered;
	}
}
