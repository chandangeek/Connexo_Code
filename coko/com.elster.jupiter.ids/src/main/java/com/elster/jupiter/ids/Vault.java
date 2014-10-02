package com.elster.jupiter.ids;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.TimeZone;

public interface Vault {
	String getComponentName();
	long getId();	
	String getDescription();
	void setDescription(String description);
	Instant getMinDate();
	Instant getMaxDate();
	boolean isRegular();
	boolean hasJournal();
	int getSlotCount();
	int getTextSlotCount();
	boolean hasLocalTime();
	boolean isPartitioned();
	boolean isActive();
	void activate(Instant to);
	void addPartition(Instant to);
	default TimeSeries createRegularTimeSeries(RecordSpec spec , TimeZone timeZone , TemporalAmount interval , int hourOffset) {
		return createRegularTimeSeries(spec, timeZone.toZoneId(), interval, hourOffset);
	}
	default TimeSeries createIrregularTimeSeries(RecordSpec spec, TimeZone timeZone) {
		return createIrregularTimeSeries(spec, timeZone.toZoneId());
	}
	TimeSeries createRegularTimeSeries(RecordSpec spec , ZoneId zoneId , TemporalAmount interval , int hourOffset);
	TimeSeries createIrregularTimeSeries(RecordSpec spec, ZoneId zoneId);
	boolean isValidInstant(Instant instant);
	void persist();
}
