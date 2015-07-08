package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Collection;
import java.util.Optional;

public enum ReadingModificationFlag {

    ADDED,
    EDITED,
    REMOVED;

    public static ReadingModificationFlag getModificationFlag(ReadingRecord reading) {
        return getModificationFlag(Optional.of(reading), reading.getReadingQualities());
    }

    public static ReadingModificationFlag getModificationFlag(Optional<? extends BaseReadingRecord> reading, Collection<? extends ReadingQuality> readingQualities) {
        if (reading.isPresent() && reading.get().wasAdded()) {
            return ADDED;
        }
        if (reading.isPresent() && reading.get().edited() &&
                readingQualities.stream().anyMatch(quality -> quality.getType().qualityIndex().orElse(null) == QualityCodeIndex.EDITGENERIC)) {
            return EDITED;
        }
        if (!reading.isPresent() && readingQualities.stream().anyMatch(quality -> quality.getType().qualityIndex().orElse(null) == QualityCodeIndex.REJECTED)) {
            return REMOVED;
        }
        return null;
    }
}