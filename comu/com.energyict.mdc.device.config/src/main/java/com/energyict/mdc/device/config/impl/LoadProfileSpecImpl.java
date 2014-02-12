package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotChangeDeviceConfigurationReferenceException;
import com.energyict.mdc.device.config.exceptions.CannotChangeLoadProfileTypeOfLoadProfileSpecException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteLoadProfileSpecLinkedChannelSpecsException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigIsRequiredException;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeIsRequiredException;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 10:24
 */
public class LoadProfileSpecImpl extends PersistentIdObject<LoadProfileSpec> implements LoadProfileSpec {

    private final DeviceConfigurationService deviceConfigurationService;
    private LoadProfileType loadProfileType;
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private DeviceConfiguration deviceConfiguration;

    @Inject
    public LoadProfileSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, DeviceConfigurationService deviceConfigurationService) {
        super(LoadProfileSpec.class, dataModel, eventService, thesaurus);
        this.deviceConfigurationService = deviceConfigurationService;
    }

    private LoadProfileSpecImpl initialize(DeviceConfiguration deviceConfig, LoadProfileType loadProfileType) {
        setDeviceConfiguration(deviceConfig);
        setLoadProfileType(loadProfileType);
        return this;
    }

    @Override
    public LoadProfileType getLoadProfileType() {
        return this.loadProfileType;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration;
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
        return getLoadProfileType().getObisCode();
    }

    @Override
    public TimeDuration getInterval() {
        return getLoadProfileType().getInterval();
    }

    private void validateRequiredFields() {
        validateDeviceConfiguration();
        validateLoadProfileType();
        validateActiveConfig();
        validateDeviceTypeContainsLoadProfileType();
    }

    private void validateDeviceTypeContainsLoadProfileType() {
        DeviceType deviceType = getDeviceConfiguration().getDeviceType();
        if (!deviceType.getLoadProfileTypes().contains(getLoadProfileType())) {
            throw new LoadProfileTypeIsNotConfiguredOnDeviceTypeException(this.thesaurus, getLoadProfileType());
        }
    }

    @Override
    protected void postNew() {
        this.getDataMapper().persist(this);
    }

    private void validateActiveConfig() {
        if (getDeviceConfiguration().getActive()) {
            throw CannotAddToActiveDeviceConfigurationException.aNewLoadProfileSpec(this.thesaurus);
        }
    }

    @Override
    protected void post() {
        this.getDataMapper().update(this);
    }

    @Override
    protected void doDelete() {
        this.getDeviceConfiguration().deleteLoadProfileSpec(this);
    }

    @Override
    public void delete() {
        getDeviceConfiguration().deleteLoadProfileSpec(this);
    }

    @Override
    public void validateDelete() {
        if (this.deviceConfigurationService.findChannelSpecsForLoadProfileSpec(this).size() > 0) {
            throw new CannotDeleteLoadProfileSpecLinkedChannelSpecsException(this.thesaurus);
        }
    }

    @Override
    public String toString() {
        return getDeviceConfiguration().getName() + "/" + getLoadProfileType().getName();
    }

    @Override
    public void setLoadProfileType(LoadProfileType loadProfileType) {
        validateLoadProfileTypeForUpdate(loadProfileType);
        this.loadProfileType = loadProfileType;
    }

    private void validateLoadProfileTypeForUpdate(LoadProfileType loadProfileType) {
        if (this.loadProfileType != null && !this.loadProfileType.equals(loadProfileType)) {
            throw new CannotChangeLoadProfileTypeOfLoadProfileSpecException(this.thesaurus);
        }
    }

    private void validateLoadProfileType() {
        if (this.loadProfileType == null) {
            throw LoadProfileTypeIsRequiredException.loadProfileSpecRequiresLoadProfileType(this.thesaurus);
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

    @Override
    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        validateDeviceConfigurationForUpdate(deviceConfiguration);
        this.deviceConfiguration = deviceConfiguration;
    }

    private void validateDeviceConfigurationForUpdate(DeviceConfiguration deviceConfiguration) {
        if (this.deviceConfiguration != null && !this.deviceConfiguration.equals(deviceConfiguration)) {
            throw CannotChangeDeviceConfigurationReferenceException.forLoadProfileSpec(this.thesaurus, this);
        }
    }

    private void validateDeviceConfiguration() {
        if (this.deviceConfiguration == null) {
            throw DeviceConfigIsRequiredException.loadProfileSpecRequiresDeviceConfig(this.thesaurus);
        }
    }

    public static class LoadProfileSpecBuilder {

        final LoadProfileSpecImpl loadProfileSpec;

        LoadProfileSpecBuilder(Provider<LoadProfileSpecImpl> loadProfileSpecProvider, DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType) {
            this.loadProfileSpec = loadProfileSpecProvider.get().initialize(deviceConfiguration, loadProfileType);
        }

        public LoadProfileSpecBuilder setOverruledObisCode(ObisCode overruledObisCode){
            this.loadProfileSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        public LoadProfileSpec add(){
            this.loadProfileSpec.validateRequiredFields();
            return this.loadProfileSpec;
        }
    }
}