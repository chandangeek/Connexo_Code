package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;

import java.util.List;

public class CimChannelAdapter extends AbstractCimChannel implements CimChannel {

    private final ChannelImpl channel;
    private final ReadingType readingType;

    CimChannelAdapter(ChannelImpl channel, ReadingType readingType, DataModel dataModel, MeteringService meteringService) {
        super(dataModel, meteringService);
        this.channel = channel;
        this.readingType = readingType;
    }

    @Override
    public ChannelImpl getChannel() {
        return channel;
    }

    @Override
    public ReadingType getReadingType() {
        return readingType;
    }

    @Override
    public void removeReadings(List<? extends BaseReadingRecord> readings) {
        //TODO automatically generated method body, provide implementation.

    }

}
