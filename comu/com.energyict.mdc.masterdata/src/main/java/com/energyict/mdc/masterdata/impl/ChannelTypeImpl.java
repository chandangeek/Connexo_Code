package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeChannelTypeUsage;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 7/15/14
 * Time: 10:01 AM
 */
@ChannelTypeHasUniqueIntervalAndRegister(groups = { Save.Create.class, Save.Update.class })
public class ChannelTypeImpl extends MeasurementTypeImpl implements ChannelType {

    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.CHANNEL_TYPE_INTERVAL_IS_REQUIRED + "}")
    private TimeDuration interval;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.CHANNEL_TYPE_SHOULD_BE_LINKED_TO_REGISTER_TYPE + "}")
    private RegisterType templateRegister;

    private long templateRegisterId;

    @Inject
    protected ChannelTypeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, MasterDataService masterDataService) {
        super(dataModel, eventService, thesaurus, clock, masterDataService);
    }

    public ChannelType initialize(RegisterType templateRegisterType, TimeDuration interval, ReadingType readingType) {
        this.setName(readingType.getName());
        this.setObisCode(templateRegisterType.getObisCode());
        this.setPhenomenon(templateRegisterType.getPhenomenon());
        this.setReadingType(readingType);
        this.setTimeOfUse(readingType.getTou());
        this.setInterval(interval);
        this.setTemplateRegister(templateRegisterType);
        return this;
    }

    @Override
    public void postLoad() {
        this.templateRegister = this.masterDataService.findRegisterType(templateRegisterId).get();
    }

    @Override
    public TimeDuration getInterval() {
        return interval;
    }

    public void setInterval(TimeDuration interval) {
        this.interval = interval;
    }

    @Override
    public RegisterType getTemplateRegister() {
        return templateRegister;
    }

    public void setTemplateRegister(RegisterType templateRegister) {
        this.templateRegister = templateRegister;
        this.templateRegisterId = this.templateRegister.getId();
    }

    protected void validateDelete() {
        this.validateNotUsedByLoadProfileTypes();
        super.validateDelete();
    }

    private void validateNotUsedByLoadProfileTypes() {
        List<LoadProfileTypeChannelTypeUsage> loadProfileTypeUsages = this.dataModel.mapper(LoadProfileTypeChannelTypeUsage.class).find("channelType", this);
        if (!loadProfileTypeUsages.isEmpty()) {
            Set<LoadProfileType> loadProfileTypes = new HashSet<>();
            for (LoadProfileTypeChannelTypeUsage loadProfileTypeUsage : loadProfileTypeUsages) {
                loadProfileTypes.add(loadProfileTypeUsage.getLoadProfileType());
            }
            throw CannotDeleteBecauseStillInUseException.channelTypeIsStillInUseByLoadprofileTypes(this.getThesaurus(), this.getTemplateRegister(), new ArrayList<>(loadProfileTypes));
        }
    }
}
