package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.exceptions.CannotChangeLogbookTypeOfLogbookSpecException;
import com.energyict.mdc.device.config.exceptions.LogbookTypeIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.LogBookType;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:30
 */
public class LogBookSpecImpl extends PersistentIdObject<LogBookSpec> implements LogBookSpec {

    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.LOGBOOK_SPEC_LOGBOOK_TYPE_IS_REQUIRED + "}")
    private final Reference<LogBookType> logBookType = ValueReference.absent();
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;

    @Inject
    public LogBookSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(LogBookSpec.class, dataModel, eventService, thesaurus);
    }

    private LogBookSpecImpl initialize(DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
        this.deviceConfiguration.set(deviceConfiguration);
        setLogBookType(logBookType);
        return this;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    public LogBookType getLogBookType() {
        return this.logBookType.get();
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
    public ObisCode getObisCode() {
        return getLogBookType().getObisCode();
    }

    @Override
    public void save() {
        validateDeviceTypeContainsLogbookType();
        super.save();
    }

    private void validateBeforeAddToConfiguration () {
        Save.CREATE.validate(this.dataModel.getValidatorFactory().getValidator(), this);
        this.validateDeviceTypeContainsLogbookType();
    }

    private void validateDeviceTypeContainsLogbookType() {
        DeviceType deviceType = getDeviceConfiguration().getDeviceType();
        if (!deviceType.getLogBookTypes().contains(getLogBookType())) {
            throw new LogbookTypeIsNotConfiguredOnDeviceTypeException(this.thesaurus, getLogBookType());
        }
    }

    private void validateUpdate () {
        Save.UPDATE.validate(this.dataModel.getValidatorFactory().getValidator(), this);
    }

    @Override
    protected void doDelete() {
        this.getDeviceConfiguration().deleteLogBookSpec(this);
    }

    @Override
    public void validateDelete() {
        // the configuration will validate the 'active' part
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.LOGBOOKSPEC;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.LOGBOOKSPEC;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.LOGBOOKSPEC;
    }

    @Override
    public String toString() {
        return getLogBookType().getName() + "/" + getObisCode().toString();
    }

    private void setLogBookType(LogBookType logBookType) {
        validateLogbookTypeForUpdate(logBookType);
        this.logBookType.set(logBookType);
    }

    private void validateLogbookTypeForUpdate(LogBookType loadProfileType) {
        if (this.logBookType.isPresent() && this.getLogBookType().getId() != loadProfileType.getId()) {
            throw new CannotChangeLogbookTypeOfLogbookSpecException(this.thesaurus);
        }
    }

    @Override
    public void setOverruledObisCode(ObisCode overruledObisCode) {
        if (overruledObisCode != null) {
            this.overruledObisCodeString = overruledObisCode.toString();
        } else {
            this.overruledObisCodeString = "";
        }
        this.overruledObisCode = overruledObisCode;
    }

    abstract static class LogBookSpecBuilder implements LogBookSpec.LogBookSpecBuilder {

        final LogBookSpecImpl logBookSpec;

        LogBookSpecBuilder(Provider<LogBookSpecImpl> logBookSpecProvider, DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
            this.logBookSpec = logBookSpecProvider.get().initialize(deviceConfiguration, logBookType);
        }

        @Override
        public LogBookSpec.LogBookSpecBuilder setOverruledObisCode(ObisCode overruledObisCode){
            this.logBookSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public LogBookSpecImpl add(){
            this.logBookSpec.validateBeforeAddToConfiguration();
            return this.logBookSpec;
        }
    }

    abstract static class LogBookSpecUpdater implements LogBookSpec.LogBookSpecUpdater {

        final LogBookSpecImpl logBookSpec;

        LogBookSpecUpdater(LogBookSpecImpl logBookSpec) {
            this.logBookSpec = logBookSpec;
        }

        @Override
        public LogBookSpec.LogBookSpecUpdater setOverruledObisCode(ObisCode overruledObisCode) {
            this.logBookSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public void update() {
            this.logBookSpec.validateUpdate();
            this.logBookSpec.save();
        }
    }
}
