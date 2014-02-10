package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Provider;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotChangeDeviceConfigurationReferenceException;
import com.energyict.mdc.device.config.exceptions.CannotChangeLogbookTypeOfLogbookSpecException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigIsRequiredException;
import com.energyict.mdc.device.config.exceptions.LogbookTypeIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.LogbookTypeIsRequiredException;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:30
 */
public class LogBookSpecImpl extends PersistentIdObject<LogBookSpec> implements LogBookSpec {

    private DeviceConfiguration deviceConfiguration;
    private LogBookType logBookType;
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;

    @Inject
    public LogBookSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(LogBookSpec.class, dataModel, eventService, thesaurus);
    }

    private LogBookSpecImpl initialize(DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
        setDeviceConfiguration(deviceConfiguration);
        setLogBookType(logBookType);
        return this;
    }

    @Override
    public DeviceConfiguration getDeviceConfig() {
        return this.deviceConfiguration;
    }

    @Override
    public LogBookType getLogBookType() {
        return this.logBookType;
    }

    @Override
    public ObisCode getDeviceObisCode() {
        if (!"".equals(this.overruledObisCodeString) && this.overruledObisCodeString != null) {
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
        validateActiveConfig();
        validateDeviceTypeContainsLogbookType();
    }

    private void validateDeviceTypeContainsLogbookType() {
        DeviceType deviceType = getDeviceConfig().getDeviceType();
        if (!deviceType.getLogBookTypes().contains(getLogBookType())) {
            throw new LogbookTypeIsNotConfiguredOnDeviceTypeException(this.thesaurus, getLogBookType());
        }
    }

    private void validateLogbookType() {
        if (this.logBookType == null) {
            throw LogbookTypeIsRequiredException.logBookSpecRequiresLoadProfileType(this.thesaurus);
        }
    }

    private void validateDeviceConfiguration() {
        if (this.deviceConfiguration == null) {
            throw DeviceConfigIsRequiredException.logBookSpecRequiresDeviceConfig(this.thesaurus);
        }
    }

    private void validateActiveConfig() {
        if (getDeviceConfig().getActive()) {
            throw CannotAddToActiveDeviceConfigurationException.aNewLoadProfileSpec(this.thesaurus);
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
        this.getDataMapper().remove(this);
    }

    @Override
    public void validateDelete() {
        // the configuration will validate the 'active' part
    }

    @Override
    public String toString() {
        return getLogBookType().getName() + "/" + getObisCode().toString();
    }

    @Override
    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        validateDeviceConfigurationForUpdate(deviceConfiguration);
        this.deviceConfiguration = deviceConfiguration;
    }


    @Override
    public void setLogBookType(LogBookType logBookType) {
        validateLogbookTypeForUpdate(logBookType);
        this.logBookType = logBookType;
    }

    private void validateLogbookTypeForUpdate(LogBookType loadProfileType) {
        if (this.logBookType != null && !this.logBookType.equals(loadProfileType)) {
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
        if(this.deviceConfiguration != null && !this.deviceConfiguration.equals(deviceConfiguration)){
            throw CannotChangeDeviceConfigurationReferenceException.forLogbookSpec(this.thesaurus, this);
        }
    }

    public static class LogBookSpecBuilder {

        final LogBookSpecImpl logBookSpec;

        LogBookSpecBuilder(Provider<LogBookSpecImpl> logBookSpecProvider, DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
            this.logBookSpec = logBookSpecProvider.get().initialize(deviceConfiguration, logBookType);
        }

        public LogBookSpecBuilder setOverruledObisCode(ObisCode overruledObisCode){
            this.logBookSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        public LogBookSpecImpl add(){
            this.logBookSpec.validateRequiredFields();
            return this.logBookSpec;
        }

    }
}
