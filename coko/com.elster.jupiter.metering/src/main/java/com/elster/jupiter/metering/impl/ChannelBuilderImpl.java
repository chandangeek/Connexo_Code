package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelBuilderImpl implements ChannelBuilder {

    private MeterActivation meterActivation;
    private List<ReadingType> readingTypes = new ArrayList<>();
    private final DataModel dataModel;

    @Inject
    public ChannelBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ChannelBuilder meterActivation(MeterActivation meterActivation) {
        this.meterActivation = meterActivation;
        return this;
    }

    @Override
    public ChannelBuilder readingTypes(ReadingType main, ReadingType... readingTypes) {
        this.readingTypes.add(main);
        this.readingTypes.addAll(Arrays.asList(readingTypes));
        return this;
    }

    @Override
    public Channel build() {
        ChannelImpl channel = ChannelImpl.from(dataModel, meterActivation);
        channel.init(readingTypes);
        return channel;
    }
}