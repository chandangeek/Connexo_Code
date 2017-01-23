package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.rest.util.IdWithNameInfo;
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
    REMOVED(QualityCodeIndex.REJECTED),
    RESET(QualityCodeIndex.REJECTED);

    private final QualityCodeIndex matchingIndex;

    ReadingModificationFlag(QualityCodeIndex matchingIndex) {
        this.matchingIndex = matchingIndex;
    }

    private static Optional<ReadingModificationFlag> forQualityCodeIndex(QualityCodeIndex index) {
        return Arrays.stream(ReadingModificationFlag.values())
                .filter(flag -> flag.matchingIndex.equals(index))
                .findFirst();
    }

    public static Pair<ReadingModificationFlag, ReadingQualityRecord> getModificationFlagWithQualityRecord(BaseReadingRecord reading, Collection<? extends ReadingQualityRecord> readingQualities, Optional<? extends BaseReadingRecord> calculatedReading) {
        Map<ReadingModificationFlag, ReadingQualityRecord> flags = readingQualities.stream()
                .filter(quality -> quality.getType().qualityIndex().flatMap(ReadingModificationFlag::forQualityCodeIndex).isPresent())
                .collect(Collectors.toMap(
                        quality -> ReadingModificationFlag.forQualityCodeIndex(quality.getType().qualityIndex().get()).get(),
                        quality -> quality,
                        (s1, s2) -> s1,
                        () -> new EnumMap<>(ReadingModificationFlag.class)));
        if (reading != null) {
            if (reading.edited()) {
                if (flags.containsKey(ADDED)) {
                    if(calculatedReading.isPresent()){
                        return Pair.of(EDITED, flags.get(ADDED));
                    } else {
                        return Pair.of(ADDED, flags.get(ADDED));
                    }
                }
                if (flags.containsKey(EDITED)) {
                    if(calculatedReading.isPresent()){
                        return Pair.of(EDITED, flags.get(EDITED));
                    } else {
                        return Pair.of(ADDED, flags.get(EDITED));
                    }
                }
            } else if (flags.containsKey(REMOVED)){
                return Pair.of(RESET, flags.get(REMOVED));
            }
        } else if (flags.containsKey(REMOVED)) {
            if(calculatedReading.isPresent()){
                return Pair.of(REMOVED, flags.get(REMOVED));
            }
        }
        return null;
    }

    public static IdWithNameInfo getApplicationInfo(QualityCodeSystem system) {
        switch (system) {
            case MDC:
                return new IdWithNameInfo(system.name(), "MultiSense");
            case MDM:
                return new IdWithNameInfo(system.name(), "Insight");
            default:
                return new IdWithNameInfo(system.name(), system.name());
        }
    }
}
