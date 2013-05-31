package com.elster.jupiter.metering;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface BaseReading {
	Date getTimeStamp();
	Date getReportedDateTime();
	BigDecimal getValue();
	BigDecimal getValue(int offset);
	BigDecimal getValue(ReadingType readingType);
	ReadingType getReadingType();
	ReadingType getReadingType(int offset);
	List<ReadingType> getReadingTypes();
	long getProcessingFlags();
}
