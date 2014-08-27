package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.units.Quantity;

import java.util.List;

public interface BaseReadingRecord extends BaseReading {
    List<Quantity> getQuantities();

    Quantity getQuantity(int offset);

    Quantity getQuantity(ReadingType readingType);

    ReadingType getReadingType();

    ReadingType getReadingType(int offset);

    List<? extends ReadingType> getReadingTypes();

    ProcessStatus getProcesStatus();

    void setProcessingFlags(ProcessStatus.Flag... flags);
    
    @Override
    List<? extends ReadingQualityRecord> getReadingQualities();
    
}
