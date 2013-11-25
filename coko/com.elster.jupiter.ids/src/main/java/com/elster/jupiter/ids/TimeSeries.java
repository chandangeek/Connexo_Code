package com.elster.jupiter.ids;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.common.base.Optional;

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
    List<TimeSeriesEntry> getEntries(Date from , Date to);
    Optional<TimeSeriesEntry> getEntry(Date when);
	boolean isValidDateTime(Date date);
	TimeZone getTimeZone();
}
