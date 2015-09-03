package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;

public enum ReadingModificationFlag {

    ADDED(QualityCodeIndex.ADDED),
    EDITED(QualityCodeIndex.EDITGENERIC),
    REMOVED(QualityCodeIndex.REJECTED);

    private final QualityCodeIndex matchingIndex;

    ReadingModificationFlag(QualityCodeIndex matchingIndex) {
        this.matchingIndex = matchingIndex;
    }

    private static Optional<ReadingModificationFlag> forQualityCodeIndex(QualityCodeIndex index) {
        return Arrays.stream(ReadingModificationFlag.values())
                .filter(flag -> flag.matchingIndex.equals(index))
                .findFirst();
    }

    public static ReadingModificationFlag getModificationFlag(ReadingRecord reading) {
        return getModificationFlag(reading, reading.getReadingQualities());
    }

    public static ReadingModificationFlag getModificationFlag(BaseReadingRecord reading, Collection<? extends ReadingQuality> readingQualities) {
        Set<ReadingModificationFlag> flags = readingQualities.stream()
                .map(ReadingQuality::getType)
                .map(ReadingQualityType::qualityIndex)
                .flatMap(asStream())
                .map(ReadingModificationFlag::forQualityCodeIndex)
                .flatMap(asStream())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ReadingModificationFlag.class)));
        if (reading != null) {
            if (reading.edited()) {
                if (flags.contains(ADDED)) {
                    return ADDED;
                }
                if (flags.contains(EDITED)) {
                    return EDITED;
                }
            }
        } else if (flags.contains(REMOVED)) {
            return REMOVED;
        }
        return null;
    }
}