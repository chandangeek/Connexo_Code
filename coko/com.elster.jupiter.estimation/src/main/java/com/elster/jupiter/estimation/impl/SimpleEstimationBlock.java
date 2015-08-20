package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.collect.ImmutableList;

import java.util.List;

class SimpleEstimationBlock implements EstimationBlock {

    private final List<Estimatable> estimatables;
    private final Channel channel;
    private final ReadingType readingType;
    private ReadingQualityType readingQualityType;

    private SimpleEstimationBlock(Channel channel, ReadingType readingType, List<? extends Estimatable> estimatables) {
        this.channel = channel;
        this.readingType = readingType;
        this.estimatables = ImmutableList.copyOf(estimatables);
    }

    public static EstimationBlock of(Channel channel, ReadingType readingType, List<? extends Estimatable> readings) {
        return new SimpleEstimationBlock(channel, readingType, readings);
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public CimChannel getCimChannel() {
        return getChannel().getCimChannel(getReadingType()).get();
    }

    @Override
    public ReadingType getReadingType() {
        return readingType;
    }

    @Override
    public List<? extends Estimatable> estimatables() {
        return estimatables;
    }

    @Override
    public void setReadingQualityType(ReadingQualityType readingQualityType) {
        this.readingQualityType = readingQualityType;
    }

    @Override
    public ReadingQualityType getReadingQualityType() {
        return readingQualityType;
    }
}
