package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.List;

public class ReadingTypeInChannel extends AbstractCimChannel {

    @SuppressWarnings("unused")
    private int position;
    
    private final Reference<ChannelImpl> channel = ValueReference.absent();
    private final Reference<ReadingTypeImpl> readingType = ValueReference.absent();

    @Inject
    ReadingTypeInChannel(DataModel dataModel, MeteringService meteringService) {
        super(dataModel, meteringService);
    }

    ReadingTypeInChannel init(ChannelImpl channel, ReadingTypeImpl readingType) {
        this.channel.set(channel);
        this.readingType.set(readingType);
        return this;
    }

    @Override
    public ReadingTypeImpl getReadingType() {
        return readingType.get();
    }

    @Override
    public ChannelImpl getChannel() {
        return channel.get();
    }

    @Override
    public void removeReadings(List<? extends BaseReadingRecord> readings) {
        //TODO automatically generated method body, provide implementation.

    }

}
