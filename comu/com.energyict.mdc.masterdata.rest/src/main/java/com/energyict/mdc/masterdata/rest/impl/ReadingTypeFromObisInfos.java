package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;

import java.util.List;

public class ReadingTypeFromObisInfos extends ReadingTypeInfos {

    // False if mapping from obis code to reading type is successful
    private final boolean mappingError;

    ReadingTypeFromObisInfos(List<ReadingType> readingTypes, boolean error){
        super(readingTypes);
        this.mappingError = error;
    }

    public boolean getMappingError(){
        return mappingError;
    }
}
