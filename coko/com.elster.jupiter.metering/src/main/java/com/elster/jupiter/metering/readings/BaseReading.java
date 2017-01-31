/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings;

import com.elster.jupiter.metering.ReadingQualityType;

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

	default boolean hasReadingQuality(ReadingQualityType readingQualityType) {
		for (ReadingQuality readingQuality : getReadingQualities()) {
			if (readingQuality.getTypeCode().equals(readingQualityType.getCode())) {
				return true;
			}
		}
		return false;
	}
}