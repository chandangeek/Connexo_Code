/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ReadingWithQualities implements Comparable {
    private Instant timestamp;
    private BaseReading reading;
    private List<ReadingQuality> readingQualities = Collections.emptyList();

    private ReadingWithQualities(Instant timestamp) {
        this.timestamp = timestamp;
    }

    static ReadingWithQualities from(BaseReading reading) {
        ReadingWithQualities rwq = new ReadingWithQualities(reading.getTimeStamp());
        rwq.reading = reading;
        return rwq;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    Optional<BaseReading> getReading() {
        return Optional.ofNullable(reading);
    }

    List<ReadingQuality> getReadingQualities() {
        Map<ReadingQualityType, ReadingQuality> qualitiesByTypes = Stream.concat(
                getReading().map(BaseReading::getReadingQualities).map(List::stream).orElseGet(Stream::empty),
                readingQualities.stream()
        ).collect(Collectors.toMap(ReadingQuality::getType, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        return new ArrayList<>(qualitiesByTypes.values());
    }

    @Override
    public int compareTo(Object reading) {
        return this.getTimestamp().compareTo(((ReadingWithQualities) reading).getTimestamp());
    }
}