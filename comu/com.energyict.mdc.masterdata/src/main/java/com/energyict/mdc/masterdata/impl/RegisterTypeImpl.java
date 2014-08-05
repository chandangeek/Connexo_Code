package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterTypeImpl extends MeasurementTypeImpl implements RegisterType {

    @Inject
    public RegisterTypeImpl(DataModel dataModel, EventService eventService, MasterDataService masterDataService, Thesaurus thesaurus, Clock clock) {
        super(dataModel, eventService, thesaurus, clock, masterDataService);
    }

    RegisterTypeImpl initialize(String name, ObisCode obisCode, Phenomenon phenomenon, ReadingType readingType, int timeOfUse) {
        this.setName(name);
        this.setObisCode(obisCode);
        this.setPhenomenon(phenomenon);
        this.setReadingType(readingType);
        this.setTimeOfUse(timeOfUse);
        return this;
    }


    @Override
    public List<RegisterGroup> getRegisterGroups() {
        Map<Long, RegisterGroup> groups = new HashMap<>();
        List<RegisterTypeInGroup> registerTypeInGroups = this.dataModel.mapper(RegisterTypeInGroup.class).find("registerType", this);
        for (RegisterTypeInGroup registerTypeInGroup : registerTypeInGroups) {
            RegisterGroup group = registerTypeInGroup.getRegisterGroup();
            if (!groups.containsKey(group.getId())) {
                groups.put(group.getId(), group);
            }
        }
        return new ArrayList<>(groups.values());
    }
}