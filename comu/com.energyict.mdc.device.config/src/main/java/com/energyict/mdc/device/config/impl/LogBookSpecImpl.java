package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.exceptions.CannotChangeDeviceConfigurationReferenceException;
import com.energyict.mdc.device.config.exceptions.CannotChangeLogbookTypeOfLogbookSpecException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigIsRequiredException;
import com.energyict.mdc.device.config.exceptions.LogbookTypeIsRequiredException;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:30
 */
public class LogBookSpecImpl extends PersistentIdObject<LogBookSpec> implements LogBookSpec {

    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
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
        validateRequiredFields();
        super.save();
    }

    private void validateRequiredFields() {
        validateDeviceConfiguration();
        validateLogbookType();
    }

    private void validateLogbookType() {
        if (!this.logBookType.isPresent()) {
            throw LogbookTypeIsRequiredException.logBookSpecRequiresLoadProfileType(this.thesaurus);
        }
    }

    private void validateDeviceConfiguration() {
        if (!this.deviceConfiguration.isPresent()) {
            throw DeviceConfigIsRequiredException.logBookSpecRequiresDeviceConfig(this.thesaurus);
        }
    }

    @Override
    protected void postNew() {
        this.getDataMapper().persist(this);
    }

    @Override
    protected void post() {
        this.getDataMapper().update(this);
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

    @Override
    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        validateDeviceConfigurationForUpdate(deviceConfiguration);
        this.deviceConfiguration.set(deviceConfiguration);
    }


    @Override
    public void setLogBookType(LogBookType logBookType) {
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

    private void validateDeviceConfigurationForUpdate(DeviceConfiguration deviceConfiguration) {
        DeviceConfiguration myDeviceConfiguration = this.getDeviceConfiguration();
        if(myDeviceConfiguration != null && myDeviceConfiguration.getId() != deviceConfiguration.getId()){
            throw CannotChangeDeviceConfigurationReferenceException.forLogbookSpec(this.thesaurus, this);
        }
    }

    static abstract class LogBookSpecBuilder implements LogBookSpec.LogBookSpecBuilder {

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
            this.logBookSpec.validateRequiredFields();
            return this.logBookSpec;
        }
    }

    static abstract class LogBookSpecUpdater implements LogBookSpec.LogBookSpecUpdater {

        final LogBookSpec logBookSpec;

        public LogBookSpecUpdater(LogBookSpec logBookSpec) {
            this.logBookSpec = logBookSpec;
        }

        @Override
        public LogBookSpec.LogBookSpecUpdater setOverruledObisCode(ObisCode overruledObisCode) {
            this.logBookSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }
    }
}
