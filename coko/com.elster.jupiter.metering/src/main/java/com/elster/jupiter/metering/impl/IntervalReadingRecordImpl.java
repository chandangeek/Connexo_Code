/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;

public class IntervalReadingRecordImpl extends BaseReadingRecordImpl implements IntervalReadingRecord {
	
	IntervalReadingRecordImpl(ChannelContract channel, TimeSeriesEntry entry) {
		super(channel,entry);
	}

	@Override
	int getReadingTypeOffset() {
		return 2;
	}
	
	@Override
	public IntervalReadingRecord filter(ReadingType readingType) {
		return new FilteredIntervalReadingRecord(this, getIndex(readingType));
	}

}
