package com.elster.jupiter.metering;

import com.elster.jupiter.util.time.Interval;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ReadingContainer {
	
	Set<ReadingType> getReadingTypes(Interval interval);
	List<? extends BaseReadingRecord> getReadings(Interval interval, ReadingType readingType);
	
	default List<? extends BaseReadingRecord> getReadingsBefore(Date when, ReadingType readingType , int count) {
		return getReadingsBefore(when.toInstant(), readingType, count);
	}
	List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType , int count);
	
	default List<? extends BaseReadingRecord> getReadingsOnOrBefore(Date when, ReadingType readingType , int count) {
		return getReadingsOnOrBefore(when.toInstant(), readingType, count);
	}
	List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType , int count);

    boolean hasData();
}
