package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.MasterDataService;

import javax.inject.Inject;

public class RegisterMappingImpl extends AbstractRegisterMappingImpl {

    @Inject
    public RegisterMappingImpl(DataModel dataModel, EventService eventService, MasterDataService masterDataService, Thesaurus thesaurus, Clock clock) {
        super(dataModel, eventService, thesaurus, clock, masterDataService);
    }

    RegisterMappingImpl initialize(String name, ObisCode obisCode, Phenomenon phenomenon, ReadingType readingType, int timeOfUse) {
        this.setName(name);
        this.setObisCode(obisCode);
        this.setPhenomenon(phenomenon);
        this.setReadingType(readingType);
        this.setTimeOfUse(timeOfUse);
        return this;
    }
}