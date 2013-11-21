package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingRecord;

public class ReadingRecordImpl extends BaseReadingRecordImpl implements ReadingRecord {
	
	ReadingRecordImpl(Channel channel, TimeSeriesEntry entry) {
		super(channel,entry);
	}

	@Override
	int getReadingTypeOffset() {
		return 1;
	}
	
	public String getReadingTypeCode() {
		return getReadingType().getMRID();
	}
	
	public String getReason() {
		return null;
	}

}
