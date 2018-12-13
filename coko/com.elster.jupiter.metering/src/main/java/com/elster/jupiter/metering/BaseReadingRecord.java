/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.units.Quantity;

import java.util.List;
import java.util.Optional;

public interface BaseReadingRecord extends BaseReading {
    List<Quantity> getQuantities();

    Quantity getQuantity(int offset);

    Quantity getQuantity(ReadingType readingType);

    ReadingType getReadingType();

    ReadingType getReadingType(int offset);

    List<? extends ReadingType> getReadingTypes();

    ProcessStatus getProcessStatus();

    void setProcessingFlags(ProcessStatus.Flag... flags);
    
    BaseReadingRecord filter(ReadingType readingType);
    
    @Override
    List<? extends ReadingQualityRecord> getReadingQualities();
    
    default boolean edited() {
        return getProcessStatus().get(ProcessStatus.Flag.EDITED);
    }
    
    default boolean wasAdded() {
        return edited() && getReadingQualities().stream()
                .map(ReadingQualityRecord::getType)
                .map(ReadingQualityType::qualityIndex)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(index -> index == QualityCodeIndex.ADDED);
    }

    default boolean confirmed() {
        return getProcessStatus().get(ProcessStatus.Flag.CONFIRMED);
    }
}
