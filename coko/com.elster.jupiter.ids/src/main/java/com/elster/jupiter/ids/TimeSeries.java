package com.elster.jupiter.ids;

import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public interface TimeSeries {
	long getId();
	Date getFirstDateTime();
	Date getLastDateTime();
	Date getLockDateTime();
	boolean isRegular();
	IntervalLength getIntervalLength();
	// offset in calendar hours (excluding DST transition hour).
	int getOffset();	
	Vault getVault();
	RecordSpec getRecordSpec();
	boolean add(Date dateTime , boolean overrule , Object... values);
    List<TimeSeriesEntry> getEntries(Interval interval);
    Optional<TimeSeriesEntry> getEntry(Date when);
    List<TimeSeriesEntry> getEntriesBefore(Date when,int entryCount);
    List<TimeSeriesEntry> getEntriesOnOrBefore(Date when,int entryCount);
	boolean isValidDateTime(Date date);
	TimeZone getTimeZone();
	void removeEntries(Range<Instant> range);
	default void removeEntry(Instant instant) {
		removeEntries(Range.closed(instant, instant));
	}
}
