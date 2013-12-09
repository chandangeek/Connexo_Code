package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.util.List;

public interface BaseReadingRecord extends BaseReading {
    List<BigDecimal> getValues();
	Quantity getValue(int offset);
	Quantity getValue(ReadingType readingType);
	ReadingType getReadingType();
	ReadingType getReadingType(int offset);
    List<ReadingType> getReadingTypes();
	long getProcessingFlags();
}
