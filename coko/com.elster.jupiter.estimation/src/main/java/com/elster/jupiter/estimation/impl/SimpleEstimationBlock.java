/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SimpleEstimationBlock implements EstimationBlock {

    private final List<Estimatable> estimatables;
    private final Channel channel;
    private final ReadingType readingType;
    private List<ReadingQualityType> readingQualityTypes = new ArrayList<>();
    private Map<ReadingQualityType, ReadingQualityComment> readingQualityTypeWithComment = new HashMap<>();

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

    void addReadingQualityType(ReadingQualityType readingQualityType, ReadingQualityComment readingQualityComment) {
        readingQualityTypeWithComment.put(readingQualityType, readingQualityComment);
    }

    @Override
    public List<ReadingQualityType> getReadingQualityTypes() {
        return Collections.unmodifiableList(readingQualityTypes);
    }

    @Override
    public Map<ReadingQualityType, ReadingQualityComment> getReadingQualityTypesWithComments() {
        return Collections.unmodifiableMap(readingQualityTypeWithComment);
    }
}
