/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.util.streams.ExtraCollectors;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class is used to push additional data about scope of new suspect readings into event queue.
 */
public class SuspectsCreatedEvent {
    private long channelsContainerId;
    private Map<Long, Range<Instant>> suspectedScope;

    /**
     * @return data transfer object containing information about scope of suspect readings
     */
    static SuspectsCreatedEvent create(ChannelsContainer channelsContainer, Collection<ReadingQualityRecord> readingQualityRecords) {
        SuspectsCreatedEvent result = new SuspectsCreatedEvent();
        result.channelsContainerId = channelsContainer.getId();
        result.suspectedScope = readingQualityRecords.stream()
                .collect(Collectors.groupingBy(rqr -> rqr.getChannel().getId(),
                        Collectors.mapping(ReadingQualityRecord::getReadingTimestamp,
                                Collectors.collectingAndThen(ExtraCollectors.<Instant>spanning(), // doesn't compile without Instant parameter
                                        Optional::get)))); // getting from Optional is OK because grouping collector can't return empty downstream
        return result;
    }

    /**
     * required for serialization
     */
    public long getChannelsContainerId() {
        return channelsContainerId;
    }

    /**
     * required for serialization
     */
    public Map<Long, Range<Instant>> getSuspectedScope() {
        return suspectedScope;
    }
}
