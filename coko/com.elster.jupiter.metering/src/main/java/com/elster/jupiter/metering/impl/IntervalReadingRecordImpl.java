package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;

public class IntervalReadingRecordImpl extends BaseReadingRecordImpl implements IntervalReadingRecord {
	
	IntervalReadingRecordImpl(Channel channel, TimeSeriesEntry entry) {
		super(channel,entry);
	}

	@Override
	public long getProfileStatus() {
		return getEntry().getLong(1);
	}

	@Override
	int getReadingTypeOffset() {
		return 2;
	}

}
