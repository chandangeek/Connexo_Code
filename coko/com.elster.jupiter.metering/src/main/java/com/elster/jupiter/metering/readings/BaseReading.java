/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings;

import com.elster.jupiter.metering.ReadingQualityType;

import com.google.common.collect.Range;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@XmlRootElement
public interface BaseReading {
	@XmlAttribute
	BigDecimal getSensorAccuracy();
	@XmlAttribute
	Instant getTimeStamp();
	@XmlAttribute
	Instant getReportedDateTime();
	@XmlAttribute
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