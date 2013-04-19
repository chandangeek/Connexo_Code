package com.elster.jupiter.ids;

import java.util.*;

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
	boolean isValidDateTime(Date date);
	TimeZone getTimeZone();
}
