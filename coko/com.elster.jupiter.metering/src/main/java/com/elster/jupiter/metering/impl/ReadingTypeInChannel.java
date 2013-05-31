package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.plumbing.Bus;

public class ReadingTypeInChannel {
	// Persistent fields
	@SuppressWarnings("unused")
	private long channelId;
	@SuppressWarnings("unused")
	private int position;
	private String readingTypeMRID;
	
	@SuppressWarnings("unused")
	private ReadingTypeInChannel() {		
	}
	
	ReadingTypeInChannel(Channel channel,ReadingType readingType, int position) {
		this.channelId = channel.getId();
		this.position = position;
		this.readingTypeMRID = readingType.getMRID();
	}

	public ReadingType getReadingType() {
		return Bus.getOrmClient().getReadingTypeFactory().get(readingTypeMRID);
	}
}
