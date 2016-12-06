package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.obis.ObisCode;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 10:24
 */
class LoadProfileSpecImpl extends PersistentIdObject<LoadProfileSpec> implements ServerLoadProfileSpec {

    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_REQUIRED + "}")
    private final Reference<LoadProfileType> loadProfileType = ValueReference.absent();
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @Valid
    private List<ChannelSpec> channelSpecs = new ArrayList<>();

    @Inject
    LoadProfileSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(LoadProfileSpec.class, dataModel, eventService, thesaurus);
    }

    private LoadProfileSpecImpl initialize(DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType) {
        this.deviceConfiguration.set(deviceConfiguration);
        this.loadProfileType.set(loadProfileType);
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

    private void validateBeforeAddToDeviceConfiguration() {
        this.validateDeviceTypeContainsLoadProfileType();
        Save.CREATE.validate(this.getDataModel(), this);
    }

    private void validateDeviceTypeContainsLoadProfileType() {
        DeviceType deviceType = getDeviceConfiguration().getDeviceType();
        if (!hasLoadProfileType(deviceType, getLoadProfileType())) {
            throw new LoadProfileTypeIsNotConfiguredOnDeviceTypeException(getLoadProfileType(), this.getThesaurus(), MessageSeeds.LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_NOT_ON_DEVICE_TYPE);
        }
    }

    private boolean hasLoadProfileType(DeviceType deviceType, LoadProfileType loadProfileType) {
        for (LoadProfileType candidate : deviceType.getLoadProfileTypes()) {
            if (candidate.getId() == loadProfileType.getId()) {
                return true;
            }
        }
        return false;
    }

    private void validateUpdate() {
        Save.UPDATE.validate(this.getDataModel(), this);
    }

    @Override
    protected void doDelete() {
        throw new UnsupportedOperationException("LoadProfileConfig is to be deleted by removing it from the device configuration");
    }

    @Override
    public void prepareDelete() {
        this.channelSpecs.clear();
    }


    @Override
    public void validateDelete() {
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
    public void setOverruledObisCode(ObisCode overruledObisCode) {
        if (overruledObisCode != null) {
            this.overruledObisCodeString = overruledObisCode.toString();
        } else {
            this.overruledObisCodeString = "";
        }
        this.overruledObisCode = overruledObisCode;
    }

    public List<ValidationRule> getValidationRules() {
        List<ReadingType> readingTypes = new ArrayList<>();
        List<ChannelType> channelTypes = this.getLoadProfileType().getChannelTypes();
        for (ChannelType mapping : channelTypes) {
            readingTypes.add(mapping.getReadingType());
        }
        return getDeviceConfiguration().getValidationRules(readingTypes);
    }

    @Override
    public List<ChannelSpec> getChannelSpecs() {
        return Collections.unmodifiableList(this.channelSpecs);
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public void addChannelSpec(ChannelSpec channelSpec) {
        this.channelSpecs.add(channelSpec);
        if (getId() > 0) {
            getDataModel().touch(this);
        }
    }

    @Override
    public void removeChannelSpec(ChannelSpec channelSpec) {
        removeFromHasIdList(channelSpecs, channelSpec);
        if (getId() > 0) {
            getDataModel().touch(this);
        }
    }

    @Override
    public LoadProfileSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        LoadProfileSpec.LoadProfileSpecBuilder builder = deviceConfiguration.createLoadProfileSpec(getLoadProfileType());
        LoadProfileSpec loadProfileSpec = builder.setOverruledObisCode(getObisCode().equals(getDeviceObisCode()) ? null : getDeviceObisCode()).add();
        getChannelSpecs().forEach(channelSpec -> {
            ChannelSpec.ChannelSpecBuilder channelSpecBuilder = deviceConfiguration.createChannelSpec(channelSpec.getChannelType(), loadProfileSpec);
            channelSpecBuilder.overruledObisCode(channelSpec.getObisCode().equals(channelSpec.getDeviceObisCode()) ? null : channelSpec.getDeviceObisCode());
            channelSpecBuilder.interval(channelSpec.getInterval());
            channelSpecBuilder.nbrOfFractionDigits(channelSpec.getNbrOfFractionDigits());
            channelSpec.getOverflow().ifPresent(channelSpecBuilder::overflow);
            if(channelSpec.isUseMultiplier()){
                channelSpecBuilder.useMultiplierWithCalculatedReadingType(channelSpec.getCalculatedReadingType().get());
            } else {
                channelSpecBuilder.noMultiplier();
            }
            channelSpecBuilder.add();
        });
        return loadProfileSpec;
    }

    abstract static class LoadProfileSpecBuilder implements LoadProfileSpec.LoadProfileSpecBuilder, ServerLoadProfileSpecBuilder {

        private final LoadProfileSpecImpl loadProfileSpec;
        private final List<BuildingCompletionListener> buildingCompletionListeners = new ArrayList<>();

        LoadProfileSpecBuilder(Provider<LoadProfileSpecImpl> loadProfileSpecProvider, DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType) {
            this.loadProfileSpec = loadProfileSpecProvider.get().initialize(deviceConfiguration, loadProfileType);
        }

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
            this.loadProfileSpec.validateBeforeAddToDeviceConfiguration();
            this.notifyListeners();
            return this.loadProfileSpec;
        }

        private void notifyListeners() {
            for (BuildingCompletionListener buildingCompletionListener : this.buildingCompletionListeners) {
                buildingCompletionListener.loadProfileSpecBuildingProcessCompleted(this.loadProfileSpec);
            }
        }
    }

    abstract static class LoadProfileSpecUpdater implements LoadProfileSpec.LoadProfileSpecUpdater {

        final LoadProfileSpecImpl loadProfileSpec;

        LoadProfileSpecUpdater(LoadProfileSpecImpl loadProfileSpec) {
            this.loadProfileSpec = loadProfileSpec;
        }

        @Override
        public LoadProfileSpec.LoadProfileSpecUpdater setOverruledObisCode(ObisCode overruledObisCode) {
            this.loadProfileSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public void update() {
            this.loadProfileSpec.validateUpdate();
            this.loadProfileSpec.save();
        }
    }

}