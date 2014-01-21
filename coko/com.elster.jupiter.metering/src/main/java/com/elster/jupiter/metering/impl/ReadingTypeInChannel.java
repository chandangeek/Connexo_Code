package com.elster.jupiter.metering.impl;

import javax.inject.Inject;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class ReadingTypeInChannel {
	
    @SuppressWarnings("unused")
    private int position;
    
    private final Reference<ChannelImpl> channel = ValueReference.absent();
    private final Reference<ReadingTypeImpl> readingType = ValueReference.absent();

    @Inject
    ReadingTypeInChannel() {
    }

    ReadingTypeInChannel init(ChannelImpl channel, ReadingTypeImpl readingType) {
        this.channel.set(channel);
        this.readingType.set(readingType);
        return this;
    }

    public ReadingTypeImpl getReadingType() {        
        return readingType.get();
    }
}
