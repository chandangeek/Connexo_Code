package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

public class ReadingTypeInChannel {
	
    @SuppressWarnings("unused")
    private int position;
    
    private final Reference<Channel> channel = ValueReference.absent();
    private final Reference<ReadingType> readingType = ValueReference.absent();

    private final DataModel dataModel;

    @Inject
    ReadingTypeInChannel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    ReadingTypeInChannel init(Channel channel, ReadingType readingType) {
        this.channel.set(channel);
        this.readingType.set(readingType);
        return this;
    }

    static ReadingTypeInChannel from(DataModel dataModel, Channel channel, ReadingType readingType) {
        return dataModel.getInstance(ReadingTypeInChannel.class).init(channel, readingType);
    }

    public ReadingType getReadingType() {        
        return readingType.get();
    }
}
