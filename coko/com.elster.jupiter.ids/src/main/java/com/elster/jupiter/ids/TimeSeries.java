package com.elster.jupiter.ids;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

public interface TimeSeries {
	long getId();
	Instant getFirstDateTime();
	Instant getLastDateTime();
	Instant getLockDateTime();
	boolean isRegular();
	TemporalAmount interval();
	// offset in calendar hours (excluding DST transition hour).
	int getOffset();	
	Vault getVault();
	RecordSpec getRecordSpec();
	boolean add(Instant dateTime , boolean overrule , Object... values);
    List<TimeSeriesEntry> getEntries(Range<Instant> interval);
    Optional<TimeSeriesEntry> getEntry(Instant when);
    List<TimeSeriesEntry> getEntriesBefore(Instant when,int entryCount);
    List<TimeSeriesEntry> getEntriesOnOrBefore(Instant when, int entryCount);
	boolean isValidInstant(Instant instant);
	ZoneId getZoneId();
	void removeEntries(Range<Instant> range);
	default void removeEntry(Instant instant) {
		removeEntries(Range.closed(instant, instant));
	}
	List<Instant> toList(Range<Instant> range);

    Instant getNextDateTime(Instant instant);
    Instant getPreviousDateTime(Instant instant);
}
