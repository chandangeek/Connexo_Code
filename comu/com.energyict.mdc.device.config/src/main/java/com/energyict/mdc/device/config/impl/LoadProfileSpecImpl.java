package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 10:24
 */
public class LoadProfileSpecImpl extends PersistentIdObject<LoadProfileSpec> implements LoadProfileSpec {

    private final DeviceConfigurationService deviceConfigurationService;
    private final Reference<LoadProfileType> loadProfileType = ValueReference.absent();
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    @Inject
    public LoadProfileSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, DeviceConfigurationService deviceConfigurationService) {
        super(LoadProfileSpec.class, dataModel, eventService, thesaurus);
        this.deviceConfigurationService = deviceConfigurationService;
    }

    private LoadProfileSpecImpl initialize(DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType) {
        this.deviceConfiguration.set(deviceConfiguration);
        setLoadProfileType(loadProfileType);
        return this;
    }

    @Override
    public LoadProfileType getLoadProfileType() {
        return this.loadProfileType.get();
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
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

    @Override
    protected void post() {
        this.getDataMapper().update(this);
    }

    @Override
    protected void doDelete() {
        this.getDeviceConfiguration().deleteLoadProfileSpec(this);
    }

    @Override
    public void validateDelete() {
        if (this.deviceConfigurationService.findChannelSpecsForLoadProfileSpec(this).size() > 0) {
            throw new CannotDeleteLoadProfileSpecLinkedChannelSpecsException(this.thesaurus);
        }
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.LOADPROFILESPEC;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.LOADPROFILESPEC;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.LOADPROFILESPEC;
    }

    @Override
    public String toString() {
        return getDeviceConfiguration().getName() + "/" + getLoadProfileType().getName();
    }

    @Override
    public void setLoadProfileType(LoadProfileType loadProfileType) {
        validateLoadProfileTypeForUpdate(loadProfileType);
        this.loadProfileType.set(loadProfileType);
    }

    private void validateLoadProfileTypeForUpdate(LoadProfileType loadProfileType) {
        if (this.loadProfileType.isPresent() && this.getLoadProfileType().getId() != loadProfileType.getId()) {
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
        this.deviceConfiguration.set(deviceConfiguration);
    }

    private void validateDeviceConfigurationForUpdate(DeviceConfiguration deviceConfiguration) {
        DeviceConfiguration myDeviceConfiguration = this.getDeviceConfiguration();
        if (myDeviceConfiguration != null && myDeviceConfiguration.getId() != deviceConfiguration.getId()) {
            throw CannotChangeDeviceConfigurationReferenceException.forLoadProfileSpec(this.thesaurus, this);
        }
    }

    private void validateDeviceConfiguration() {
        if (!this.deviceConfiguration.isPresent()) {
            throw DeviceConfigIsRequiredException.loadProfileSpecRequiresDeviceConfig(this.thesaurus);
        }
    }

    static abstract class LoadProfileSpecBuilder implements LoadProfileSpec.LoadProfileSpecBuilder {

        private final LoadProfileSpecImpl loadProfileSpec;
        private final List<BuildingCompletionListener> buildingCompletionListeners = new ArrayList<>();

        LoadProfileSpecBuilder(Provider<LoadProfileSpecImpl> loadProfileSpecProvider, DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType) {
            this.loadProfileSpec = loadProfileSpecProvider.get().initialize(deviceConfiguration, loadProfileType);
        }

        @Override
        public void notifyOnAdd(BuildingCompletionListener buildingCompletionListener) {
            this.buildingCompletionListeners.add(buildingCompletionListener);
        }

        @Override
        public LoadProfileSpec.LoadProfileSpecBuilder setOverruledObisCode(ObisCode overruledObisCode) {
            this.loadProfileSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public LoadProfileSpec add() {
            this.loadProfileSpec.validateRequiredFields();
            this.notifyListeners();
            return this.loadProfileSpec;
        }

        private void notifyListeners() {
            for (BuildingCompletionListener buildingCompletionListener : this.buildingCompletionListeners) {
                buildingCompletionListener.loadProfileSpecBuildingProcessCompleted(this.loadProfileSpec);
            }
        }
    }

    static abstract class LoadProfileSpecUpdater implements LoadProfileSpec.LoadProfileSpecUpdater {

        final LoadProfileSpec loadProfileSpec;

        protected LoadProfileSpecUpdater(LoadProfileSpec loadProfileSpec) {
            this.loadProfileSpec = loadProfileSpec;
        }

        @Override
        public LoadProfileSpec.LoadProfileSpecUpdater setOverruledObisCode(ObisCode overruledObisCode) {
            this.loadProfileSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public void update() {
            this.loadProfileSpec.save();
        }
    }

}