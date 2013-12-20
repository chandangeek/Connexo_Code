package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class ReadingTypeInChannel {
    private final Reference<Channel> channel = ValueReference.absent();
	@SuppressWarnings("unused")
	private int position;
    private transient ReadingType readingType;
    private String readingTypeMRID;

	@SuppressWarnings("unused")
	private ReadingTypeInChannel() {		
	}
	
	ReadingTypeInChannel(Channel channel, ReadingType readingType, int position) {
        this.channel.set(channel);
		this.position = position;
		this.readingTypeMRID = readingType.getMRID();
        this.readingType = readingType;
	}

	public ReadingType getReadingType() {
        if (readingType == null) {
            readingType = Bus.getOrmClient().getReadingTypeFactory().getExisting(readingTypeMRID);
        }
		return readingType;
	}
}
