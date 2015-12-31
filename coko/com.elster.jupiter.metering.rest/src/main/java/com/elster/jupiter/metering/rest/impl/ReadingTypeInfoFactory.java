package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

public class ReadingTypeInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ReadingTypeInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ReadingTypeInfo from(ReadingType readingType){
        ReadingTypeInfo info =  new ReadingTypeInfo(readingType);
        info.macroPeriod = thesaurus.getString("readingType.macroperiod."+readingType.getMacroPeriod().name(),readingType.getMacroPeriod().getDescription());
        info.aggregate = thesaurus.getString("readingType.aggregate."+readingType.getAggregate().name(),readingType.getAggregate().getDescription());
        info.measuringPeriod = thesaurus.getString("readingType.measuringperiod."+readingType.getMeasuringPeriod().name(),readingType.getMeasuringPeriod().getDescription());
        info.accumulation = thesaurus.getString("readingType.accumulation."+readingType.getAccumulation().name(),readingType.getAccumulation().getDescription());
        info.flowDirection = thesaurus.getString("readingType.flowDirection."+readingType.getFlowDirection().name(),readingType.getFlowDirection().getDescription());
        info.commodity = thesaurus.getString("readingType.commodity."+readingType.getCommodity().name(),readingType.getCommodity().getDescription());
        info.measurementKind = thesaurus.getString("readingType.measurementKind."+readingType.getMeasurementKind().name(),readingType.getMeasurementKind().getDescription());
        info.phases = thesaurus.getString("readingType.phase."+readingType.getPhases().name(),readingType.getPhases().getBaseDescription());
        info.unit = thesaurus.getString("readingType.unit."+readingType.getUnit().name(),readingType.getUnit().getSymbol());
        return info;
    }

    public ReadingTypeInfos from(Iterable<? extends ReadingType> readingTypes){
        ReadingTypeInfos readingTypeInfos = new ReadingTypeInfos();
        for (ReadingType readingType : readingTypes) {
            readingTypeInfos.add(readingType);
        }
        return readingTypeInfos;
    }
}
