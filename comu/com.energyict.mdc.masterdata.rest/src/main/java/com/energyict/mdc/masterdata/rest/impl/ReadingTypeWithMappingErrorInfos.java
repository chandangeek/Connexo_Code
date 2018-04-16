package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;

import java.util.List;

public class ReadingTypeWithMappingErrorInfos extends ReadingTypeInfos {


    private final String mappingError;

    ReadingTypeWithMappingErrorInfos(List<ReadingType> readingTypes, String error){
        super(readingTypes);
        this.mappingError = error;
    }

    public String getMappingError(){
        return mappingError;
    }
}
