package com.elster.jupiter.ids;

import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public interface TimeSeries {
	long getId();
	Date getFirstDateTime();
	Date getLastDateTime();
	Date getLockDateTime();
	boolean isRegular();
	int getIntervalLength();
	IntervalLengthUnit getIntervalLengthUnit();
	// offset in calendar hours (excluding DST transition hour).
	int getOffset();	
	Vault getVault();
	RecordSpec getRecordSpec();
	boolean add(Date dateTime , boolean overrule , Object... values);
    List<TimeSeriesEntry> getEntries(Interval interval);
    Optional<TimeSeriesEntry> getEntry(Date when);
	boolean isValidDateTime(Date date);
	TimeZone getTimeZone();
}
