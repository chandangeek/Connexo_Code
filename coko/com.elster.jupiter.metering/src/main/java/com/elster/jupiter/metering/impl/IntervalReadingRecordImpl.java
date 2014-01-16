package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.readings.ProfileStatus;

public class IntervalReadingRecordImpl extends BaseReadingRecordImpl implements IntervalReadingRecord {
	
	IntervalReadingRecordImpl(Channel channel, TimeSeriesEntry entry) {
		super(channel,entry);
	}

	@Override
	public ProfileStatus getProfileStatus() {
		return new ProfileStatus(getEntry().getLong(1));
	}

	@Override
	int getReadingTypeOffset() {
		return 2;
	}

}
