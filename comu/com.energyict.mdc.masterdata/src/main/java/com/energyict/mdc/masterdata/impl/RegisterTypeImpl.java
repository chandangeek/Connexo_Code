package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReadingTypeInterval(groups = { Save.Create.class, Save.Update.class }, measurementType = MeasurementTypeImpl.REGISTER_DISCRIMINATOR, message = "{"+ MessageSeeds.Keys.REGISTER_TYPE_SHOULD_NOT_HAVE_INTERVAL_READINGTYPE +"}")
public class RegisterTypeImpl extends MeasurementTypeImpl implements RegisterType {

    private List<RegisterGroup> registerGroups;

    @Inject
    public RegisterTypeImpl(DataModel dataModel, EventService eventService, MasterDataService masterDataService, Thesaurus thesaurus) {
        super(dataModel, eventService, thesaurus, masterDataService);
    }

    RegisterTypeImpl initialize(ObisCode obisCode, ReadingType readingType) {
        this.setObisCode(obisCode);
        this.setReadingType(readingType);
        return this;
    }

    @Override
    public List<RegisterGroup> getRegisterGroups() {
        if (this.registerGroups == null) {
            this.cacheRegisterGroups();
        }
        return this.registerGroups;
    }

    private void cacheRegisterGroups() {
        Map<Long, RegisterGroup> groups = new HashMap<>();
        List<RegisterTypeInGroup> registerTypeInGroups = this.dataModel.mapper(RegisterTypeInGroup.class).find("registerType", this);
        for (RegisterTypeInGroup registerTypeInGroup : registerTypeInGroups) {
            RegisterGroup group = registerTypeInGroup.getRegisterGroup();
            if (!groups.containsKey(group.getId())) {
                groups.put(group.getId(), group);
            }
        }
        this.registerGroups = ImmutableList.copyOf(groups.values());
    }

}