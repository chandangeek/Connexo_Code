package com.elster.jupiter.metering;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.elster.jupiter.util.time.Interval;

public interface ReadingContainer {
	
	Set<ReadingType> getReadingTypes(Interval interval);
	List<? extends BaseReadingRecord> getReadings(Interval interval, ReadingType readingType);
	List<? extends BaseReadingRecord> getReadingsBefore(Date when, ReadingType readingType , int count);
	List<? extends BaseReadingRecord> getReadingsOnOrBefore(Date when, ReadingType readingType , int count);
}
