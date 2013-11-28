package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelBuilderImpl implements ChannelBuilder {

    private MeterActivation meterActivation;
    private List<ReadingType> readingTypes = new ArrayList<>();

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
        ChannelImpl channel = new ChannelImpl(meterActivation);
        channel.init(readingTypes);
        return channel;
    }
}