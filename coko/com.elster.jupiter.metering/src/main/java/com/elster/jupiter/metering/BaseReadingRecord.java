package com.elster.jupiter.metering;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Interval;

public interface BaseReadingRecord extends BaseReading {
    List<BigDecimal> getValues();
	BigDecimal getValue(int offset);
	BigDecimal getValue(ReadingType readingType);
	ReadingType getReadingType();
	ReadingType getReadingType(int offset);
    List<ReadingType> getReadingTypes();
	long getProcessingFlags();
}
