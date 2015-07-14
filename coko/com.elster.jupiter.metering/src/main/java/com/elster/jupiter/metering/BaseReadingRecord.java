package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeIndex;
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
    
    BaseReadingRecord filter(ReadingType readingType);
    
    @Override
    List<? extends ReadingQualityRecord> getReadingQualities();
    
    default boolean edited() {
    	return getProcesStatus().get(ProcessStatus.Flag.EDITED);
    }
    
    default boolean wasAdded() {
    	return edited() && 
    		getReadingQualities().stream().anyMatch(quality -> quality.getType().qualityIndex().orElse(null) == QualityCodeIndex.ADDED);     			
    }

    default boolean confirmed() {
        return getProcesStatus().get(ProcessStatus.Flag.CONFIRMED);
    }
}
