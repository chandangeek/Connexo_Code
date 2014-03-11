package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdatePhenomenonWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static com.elster.jupiter.util.Checks.is;

public class RegisterMappingImpl extends PersistentNamedObject<RegisterMapping> implements RegisterMapping {

    private final DeviceConfigurationService deviceConfigurationService;

    private ObisCode obisCode;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED_KEY + "}")
    private String obisCodeString;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.UNIT_IS_REQUIRED_KEY + "}")
    private Reference<Phenomenon> phenomenon = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.READING_TYPE_IS_REQUIRED_KEY + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();
    private boolean cumulative;
    private Reference<RegisterGroup> registerGroup = ValueReference.absent();
    private String description;
    private Date modificationDate;
    @Min(value=0, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.TIMEOFUSE_TOO_SMALL + "}")
    private int timeOfUse;

    private Clock clock;

    @Inject
    public RegisterMappingImpl(DataModel dataModel, EventService eventService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus, Clock clock) {
        super(RegisterMapping.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    RegisterMappingImpl initialize(String name, ObisCode obisCode, Phenomenon phenomenon, ReadingType readingType, int timeOfUse) {
        this.setName(name);
        this.setObisCode(obisCode);
        this.setPhenomenon(phenomenon);
        this.setReadingType(readingType);
        this.setTimeOfUse(timeOfUse);
        return this;
    }

    @Override
    public void save () {
        validateUniqueObisCodeAndPhenomenonAndTimeOfUse();
        this.modificationDate = this.clock.now();
        super.save();
    }

    private void validateUniqueObisCodeAndPhenomenonAndTimeOfUse() {
        if (this.phenomenon.isPresent() && this.obisCode != null) {
            RegisterMapping otherRegisterMapping = this.findOtherByObisCodeAndPhenomenonAndTimeOfUse();
            if (otherRegisterMapping != null) {
                throw DuplicateObisCodeException.forRegisterMapping(this.getThesaurus(), obisCode, phenomenon.get()
                        , timeOfUse, otherRegisterMapping);
            }
        }
    }

    protected void post() {
        this.validateDeviceConfigurations();
        super.post();
    }

    private void validateDeviceConfigurations() {
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingRegisterMapping(this);
        for (DeviceConfiguration each : deviceConfigurations) {
            ServerDeviceConfiguration deviceConfiguration = (ServerDeviceConfiguration) each;
            deviceConfiguration.validateUpdateRegisterMapping(this);
        }
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.REGISTERMAPPING;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.REGISTERMAPPING;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.REGISTERMAPPING;
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.registerMappingAlreadyExists(this.getThesaurus(), name);
    }

    public ObisCode getObisCode() {
        if (this.obisCode == null && !is(this.obisCodeString).empty()) {
            this.obisCode = ObisCode.fromString(this.obisCodeString);
        }
        return this.obisCode;
    }

    @Override
    public void setObisCode(ObisCode obisCode) {
        if (obisCode == null) {
            // javax.validation will throw ConstraintValidationException in the end
            this.obisCodeString = null;
            this.obisCode = null;
        }
        else if (this.obisCodeChanged(obisCode)) {
            if (this.isInUse()) {
                throw new CannotUpdateObisCodeWhenRegisterMappingIsInUseException(this.getThesaurus(), this);
            }
            this.obisCodeString = obisCode.toString();
            this.obisCode = obisCode;
        }
    }

    private boolean obisCodeChanged(ObisCode obisCode) {
        return !is(this.obisCode).equalTo(obisCode);
    }

    private RegisterMapping findOtherByObisCodeAndPhenomenonAndTimeOfUse() {
        RegisterMapping registerMapping = this.getDataMapper().getUnique(
                new String[] { "obisCodeString",    "phenomenon",         "timeOfUse" },
                new Object[] {  obisCode.toString(), this.getPhenomenon(), this.getTimeOfUse()} ).orNull();
        if (registerMapping != null && this.getId() > 0 && registerMapping.getId() == this.getId()) {
            // The RegisterMapping that was found is the one we are updating so ignore it
            return null;
        }
        else {
            // No other RegisterMapping found or this RegisterMapping is not peristent yet or the other is really different because the id does not match
            return registerMapping;
        }
    }

    @Override
    public RegisterGroup getRegisterGroup() {
        return this.registerGroup.orNull();
    }

    @Override
    public void setRegisterGroup(RegisterGroup registerGroup) {
        this.registerGroup.set(registerGroup);
    }

    private boolean phenomenonChanged(Phenomenon phenomenon) {
        return ((!this.phenomenon.isPresent() && phenomenon != null)
            || (phenomenon != null && (this.getPhenomenon().getId() != phenomenon.getId())));
    }

    public Phenomenon getPhenomenon() {
        return phenomenon.get();
    }

    public void setPhenomenon(Phenomenon phenomenon) {
        if (phenomenon==null) {
            this.phenomenon.setNull();
        } else if (phenomenonChanged(phenomenon)) {
            if (this.isInUse()) {
                throw new CannotUpdatePhenomenonWhenRegisterMappingIsInUseException(this.getThesaurus(), this);
            }
            this.phenomenon.set(phenomenon);
        }
    }

    public void setReadingType(ReadingType readingType) {
        this.readingType.set(readingType);
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType.get();
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    protected void validateDelete() {
        this.validateNotUsedByRegisterSpecs();
        this.validateNotUsedByChannelSpecs();
        this.validateNotUsedByLoadProfileTypes();
        this.validateNotUsedByDeviceTypes();
    }

    private void validateNotUsedByRegisterSpecs() {
        List<RegisterSpec> registerSpecs = this.mapper(RegisterSpec.class).find("registerMapping", this);
        if (!registerSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByRegisterSpecs(this.getThesaurus(), this, registerSpecs);
        }
    }

    private void validateNotUsedByChannelSpecs() {
        List<ChannelSpec> channelSpecs = this.mapper(ChannelSpec.class).find("registerMapping", this);
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByChannelSpecs(this.getThesaurus(), this, channelSpecs);
        }
    }

    private void validateNotUsedByLoadProfileTypes() {
        List<LoadProfileTypeRegisterMappingUsage> loadProfileTypeUsages = this.mapper(LoadProfileTypeRegisterMappingUsage.class).find("registerMapping", this);
        if (!loadProfileTypeUsages.isEmpty()) {
            Set<LoadProfileType> loadProfileTypes = new HashSet<>();
            for (LoadProfileTypeRegisterMappingUsage loadProfileTypeUsage : loadProfileTypeUsages) {
                loadProfileTypes.add(loadProfileTypeUsage.getLoadProfileType());
            }
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByLoadprofileTypes(this.getThesaurus(), this, new ArrayList<>(loadProfileTypes));
        }
    }

    private void validateNotUsedByDeviceTypes() {
        List<DeviceTypeRegisterMappingUsage> deviceTypeUsages = this.mapper(DeviceTypeRegisterMappingUsage.class).find("registerMapping", this);
        if (!deviceTypeUsages.isEmpty()) {
            Set<DeviceType> deviceTypes = new HashSet<>();
            for (DeviceTypeRegisterMappingUsage deviceTypeUsage : deviceTypeUsages) {
                deviceTypes.add(deviceTypeUsage.getDeviceType());
            }
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByDeviceTypes(this.getThesaurus(), this, new ArrayList<>(deviceTypes));
        }
    }

    @Override
    public boolean isInUse() {
        return this.usedByChannelSpecs() || this.usedByRegisterSpecs();
    }

    @Override
    public boolean isLinkedByDeviceType() {
        return deviceConfigurationService.isRegisterMappingUsedByDeviceType(this);
    }

    private boolean usedByChannelSpecs() {
        List<ChannelSpec> channelSpecs = this.mapper(ChannelSpec.class).find("registerMapping", this);
        return !channelSpecs.isEmpty();
    }

    private boolean usedByRegisterSpecs() {
        List<RegisterSpec> registerSpecs = this.mapper(RegisterSpec.class).find("registerMapping", this);
        return !registerSpecs.isEmpty();
    }

    @Override
    public boolean isCumulative() {
        return this.cumulative;
    }

    @Override
    public void setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
    }

    @Override
    public Unit getUnit() {
        return this.phenomenon.get().getUnit();
    }

    @Override
    public void setUnit(Unit unit) {
        Phenomenon phenomenon = dataModel.mapper(Phenomenon.class).getUnique("unitString", unit.dbString()).orNull();
        setPhenomenon(phenomenon);
    }

    public Date getModificationDate() {
        return this.modificationDate;
    }

    @Override
    public int getTimeOfUse() {
        return timeOfUse;
    }

    @Override
    public void setTimeOfUse(int timeOfUse) {
        this.timeOfUse = timeOfUse;
    }
}