package com.elster.jupiter.ids;

import java.util.*;

public interface Vault {
	String getComponentName();
	long getId();	
	String getDescription();
	void setDescription(String description);
	Date getMinDate();
	Date getMaxDate();
	boolean isRegular();
	boolean hasJournal();
	int getSlotCount();
	int getTextSlotCount();
	boolean hasLocalTime();
	boolean isPartitioned();
	boolean isActive();
	void activate(Date to);
	void addPartition(Date to);
	TimeSeries createRegularTimeSeries(RecordSpec spec , TimeZone timeZone , IntervalLength intervalLength , int hourOffset);
	TimeSeries createIrregularTimeSeries(RecordSpec spec, TimeZone timeZone);
	boolean isValidDateTime(Date date);
	void persist();
}
