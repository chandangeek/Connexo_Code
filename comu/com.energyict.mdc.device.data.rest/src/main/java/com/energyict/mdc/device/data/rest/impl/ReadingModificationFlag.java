/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public static Pair<ReadingModificationFlag, QualityCodeSystem> getModificationFlag(ReadingRecord reading) {
        return getModificationFlag(reading, reading.getReadingQualities());
    }

    public static Pair<ReadingModificationFlag, QualityCodeSystem> getModificationFlag(BaseReadingRecord reading, Collection<? extends ReadingQuality> readingQualities) {
        Map<ReadingModificationFlag, QualityCodeSystem> flags = readingQualities.stream()
                .map(ReadingQuality::getType)
                .filter(type -> type.qualityIndex().flatMap(ReadingModificationFlag::forQualityCodeIndex).isPresent())
                .collect(Collectors.toMap(
                        type -> ReadingModificationFlag.forQualityCodeIndex(type.qualityIndex().get()).get(),
                        type -> type.system().orElse(null),
                        (s1, s2) -> s1,
                        () -> new EnumMap<>(ReadingModificationFlag.class)));
        if (reading != null) {
            if (reading.edited()) {
                if (flags.containsKey(ADDED)) {
                    return Pair.of(ADDED, flags.get(ADDED));
                }
                if (flags.containsKey(EDITED)) {
                    return Pair.of(EDITED, flags.get(EDITED));
                }
            }
        } else if (flags.containsKey(REMOVED)) {
            return Pair.of(REMOVED, flags.get(REMOVED));
        }
        return null;
    }
}
