package com.elster.jupiter.metering.readings;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BaseReading {
	BigDecimal getSensorAccuracy();
	Instant getTimeStamp();
	Instant getReportedDateTime();
	BigDecimal getValue();
	String getSource();
	Optional<Range<Instant>> getTimePeriod();
	List<? extends ReadingQuality> getReadingQualities();
}
