package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Reading;

public class ReadingImpl extends BaseReadingImpl implements Reading {
	
	ReadingImpl(Channel channel, TimeSeriesEntry entry) {
		super(channel,entry);
	}

	@Override
	int getReadingTypeOffset() {
		return 1;
	}

}
