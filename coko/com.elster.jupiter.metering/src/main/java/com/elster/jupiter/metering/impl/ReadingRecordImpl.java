/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;

public class ReadingRecordImpl extends BaseReadingRecordImpl implements ReadingRecord {
	
	ReadingRecordImpl(ChannelImpl channel, TimeSeriesEntry entry) {
		super(channel,entry);
	}

	@Override
	final int getReadingTypeOffset() {
		return 1;
	}
	
	public String getReadingTypeCode() {
		return getReadingType().getMRID();
	}
	
	public String getReason() {
		return null;
	}

	@Override
	public String getText() {
		return getEntry().getString(getReadingTypeOffset()+1);
	}

	@Override
	public ReadingRecord filter(ReadingType readingType) {
		return new FilteredReadingRecord(this, getIndex(readingType));
	}
}
