package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Collection;

public enum ReadingModificationFlag {

    ADDED,
    EDITED,
    REMOVED;

    public static ReadingModificationFlag getModificationFlag(ReadingRecord reading) {
        return getModificationFlag(reading, reading.getReadingQualities());
    }

    public static ReadingModificationFlag getModificationFlag(BaseReadingRecord reading, Collection<? extends ReadingQuality> readingQualities) {
        if (reading != null) {
            if (reading.wasAdded()) {
                return ADDED;
            }
            if (reading.edited() &&
                    readingQualities.stream().anyMatch(quality -> quality.getType().qualityIndex().orElse(null) == QualityCodeIndex.EDITGENERIC)) {
                return EDITED;
            }
        } else if (readingQualities.stream().anyMatch(quality -> quality.getType().qualityIndex().orElse(null) == QualityCodeIndex.REJECTED)) {
            return REMOVED;
        }
        return null;
    }
}