/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;

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

class ReadingWithQualities {
    private Instant timestamp;
    private BaseReadingRecord reading;
    private List<ReadingQualityRecord> readingQualities = Collections.emptyList();

    private ReadingWithQualities(Instant timestamp) {
        this.timestamp = timestamp;
    }

    static ReadingWithQualities missing(Instant timestamp) {
        return new ReadingWithQualities(timestamp);
    }

    static ReadingWithQualities from(BaseReadingRecord reading) {
        ReadingWithQualities rwq = new ReadingWithQualities(reading.getTimeStamp());
        rwq.reading = reading;
        return rwq;
    }

    void setReadingQualities(List<ReadingQualityRecord> readingQualities) {
        this.readingQualities = readingQualities;
    }

    Instant getTimestamp() {
        return timestamp;
    }

    Optional<BaseReadingRecord> getReading() {
        return Optional.ofNullable(reading);
    }

    List<ReadingQualityRecord> getReadingQualities() {
        Map<ReadingQualityType, ReadingQualityRecord> qualitiesByTypes = Stream.concat(
                getReading().map(BaseReadingRecord::getReadingQualities).map(List::stream).orElseGet(Stream::empty),
                readingQualities.stream()
        )
                .collect(Collectors.toMap(ReadingQualityRecord::getType, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        return new ArrayList<>(qualitiesByTypes.values());
    }
}
