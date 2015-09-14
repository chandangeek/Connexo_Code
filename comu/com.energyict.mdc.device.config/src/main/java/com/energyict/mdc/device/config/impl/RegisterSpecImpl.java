package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.RegisterTypeIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class RegisterSpecImpl<T extends RegisterSpec> extends PersistentIdObject<T> implements ServerRegisterSpec {

    protected static final String NUMERICAL_DISCRIMINATOR = "0";
    protected static final String TEXTUAL_DISCRIMINATOR = "1";
    static final Map<String, Class<? extends RegisterSpec>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends RegisterSpec>>of(
                    NUMERICAL_DISCRIMINATOR, NumericalRegisterSpecImpl.class,
                    TEXTUAL_DISCRIMINATOR, TextualRegisterSpecImpl.class);

    private final Reference<DeviceConfiguration> deviceConfig = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_REGISTER_TYPE_IS_REQUIRED + "}")
    private final Reference<RegisterType> registerType = ValueReference.absent();
    @Size(max = 80, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public RegisterSpecImpl(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(domainClass, dataModel, eventService, thesaurus);
    }

    protected RegisterSpecImpl initialize(DeviceConfiguration configuration, RegisterType registerType) {
        this.setDeviceConfig(configuration);
        this.setRegisterType(registerType);
        return this;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return deviceConfig.get();
    }

    @Override
    public RegisterType getRegisterType() {
        return registerType.get();
    }

    @Override
    public ObisCode getObisCode() {
        return getRegisterType().getObisCode();
    }

    @Override
    public Unit getUnit() {
        return getRegisterType().getUnit();
    }

    @Override
    public ObisCode getDeviceObisCode() {
        if (!Checks.is(this.overruledObisCodeString).empty()) {
            this.overruledObisCode = ObisCode.fromString(this.overruledObisCodeString);
            return overruledObisCode;
        }
        return getObisCode();
    }

    @Override
    public boolean isTextual() {
        return false;
    }

    @Override
    public Instant getModificationDate() {
        return this.modTime;
    }

    protected void validateBeforeAddToConfiguration() {
        Save.CREATE.validate(this.getDataModel(), this);
        this.validate();
    }

    protected void validate() {
        this.validateDeviceTypeContainsRegisterType();
    }

    private void validateDeviceTypeContainsRegisterType() {
        DeviceType deviceType = getDeviceConfiguration().getDeviceType();
        boolean notFound = true;
        for (MeasurementType mapping : deviceType.getRegisterTypes()) {
            if (mapping.getId()== getRegisterType().getId()) {
                notFound = false;
            }
        }
        if (notFound) {
            throw new RegisterTypeIsNotConfiguredOnDeviceTypeException(getRegisterType(), this.getThesaurus(), MessageSeeds.REGISTER_SPEC_REGISTER_TYPE_IS_NOT_ON_DEVICE_TYPE);
        }
    }

    @Override
    protected void doDelete() {
        // TODO Check that the EISRTUREGISTERREADINGS and EISRTUREGISTERS get deleted via cascading ...
        this.getDeviceConfiguration().deleteRegisterSpec(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.REGISTERSPEC;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.REGISTERSPEC;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.REGISTERSPEC;
    }

    @Override
    public void validateDelete() {
        // the configuration must validate the 'active' state
    }

    @Override
    public void validateUpdate() {
        Save.UPDATE.validate(this.getDataModel(), this);
        this.validate();
    }

    public String toString() {
        return getDeviceConfiguration().getName() + " - " + getRegisterType().getReadingType().getAliasName();
    }

    public void setDeviceConfig(DeviceConfiguration deviceConfig) {
        this.deviceConfig.set(deviceConfig);
    }

    @Override
    public void setRegisterType(RegisterType registerType) {
        this.registerType.set(registerType);
    }

    @Override
    public void setOverruledObisCode(ObisCode overruledObisCode) {
        this.overruledObisCode = overruledObisCode;
        this.overruledObisCodeString = overruledObisCode==null?null:overruledObisCode.toString();
    }

    public List<ValidationRule> getValidationRules() {
        List<ReadingType> readingTypes = new ArrayList<>();
        readingTypes.add(getRegisterType().getReadingType());
        return getDeviceConfiguration().getValidationRules(readingTypes);
    }

    @Override
    public ReadingType getReadingType() {
        return getRegisterType().getReadingType();
    }

}