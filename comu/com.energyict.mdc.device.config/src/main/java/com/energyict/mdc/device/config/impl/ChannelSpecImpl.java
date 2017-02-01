/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Range;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.exceptions.CannotChangeChannelTypeOfChannelSpecException;
import com.energyict.mdc.device.config.exceptions.CannotChangeLoadProfileSpecOfChannelSpec;
import com.energyict.mdc.device.config.exceptions.DuplicateChannelTypeException;
import com.energyict.mdc.device.config.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.device.config.exceptions.LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.RegisterTypeIsNotConfiguredException;
import com.energyict.mdc.device.config.exceptions.UnsupportedIntervalException;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MeasurementType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.util.Checks.is;

@ValidChannelSpecMultiplierConfiguration(groups = {Save.Create.class, Save.Update.class})
@ValidateUpdatableChannelSpecFields(groups = {Save.Update.class})
@ChannelOverflowValueValidation(groups = {Save.Create.class, Save.Update.class})
class ChannelSpecImpl extends PersistentIdObject<ChannelSpec> implements ServerChannelSpec {

    enum ChannelSpecFields {
        DEVICE_CONFIG("deviceConfiguration"),
        CHANNEL_TYPE("channelType"),
        LOADPROFILE_SPEC("loadProfileSpec"),
        INTERVAL_CODE("interval.timeUnitCode"),
        INTERVAL_COUNT("interval.count"),
        NUMBER_OF_FRACTION_DIGITS("nbrOfFractionDigits"),
        OVERFLOW_VALUE("overflow"),
        OVERRULED_OBISCODE("overruledObisCodeString"),
        USEMULTIPLIER("useMultiplier"),
        CALCULATED_READINGTYPE("calculatedReadingType");

        private final String javaFieldName;

        ChannelSpecFields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CHANNEL_SPEC_CHANNEL_TYPE_IS_REQUIRED + "}")
    private final Reference<ChannelType> channelType = ValueReference.absent();
    private final Reference<LoadProfileSpecImpl> loadProfileSpec = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CHANNEL_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS + "}")
    @Range(min = 0, max = 6, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CHANNEL_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS + "}")
    private Integer nbrOfFractionDigits = 0;
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private BigDecimal overflow;
    private TimeDuration interval;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private boolean useMultiplier;
    private Reference<ReadingType> calculatedReadingType = ValueReference.absent();

    @Inject
    public ChannelSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ChannelSpec.class, dataModel, eventService, thesaurus);
    }

    private ChannelSpecImpl initialize(DeviceConfiguration deviceConfiguration, ChannelType channelType, LoadProfileSpecImpl loadProfileSpec) {
        this.initialize(deviceConfiguration, channelType);
        setLoadProfileSpec(loadProfileSpec);
        return this;
    }

    private ChannelSpecImpl initialize(DeviceConfiguration deviceConfiguration, ChannelType channelType) {
        this.deviceConfiguration.set(deviceConfiguration);
        setChannelType(channelType);
        return this;
    }

    @Override
    public com.energyict.mdc.masterdata.ChannelType getChannelType() {
        return channelType.get();
    }

    @Override
    public ObisCode getDeviceObisCode() {
        if (!is(this.overruledObisCodeString).empty()) {
            this.overruledObisCode = ObisCode.fromString(this.overruledObisCodeString);
            return overruledObisCode;
        }
        return getObisCode();
    }

    @Override
    public ObisCode getObisCode() {
        return getChannelType().getObisCode();
    }

    @Override
    public int getNbrOfFractionDigits() {
        return nbrOfFractionDigits;
    }

    @Override
    public Optional<BigDecimal> getOverflow() {
        return Optional.ofNullable(overflow);
    }

    @Override
    public LoadProfileSpec getLoadProfileSpec() {
        return this.loadProfileSpec.get();
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    public TimeDuration getInterval() {
        return (this.loadProfileSpec.isPresent() ? getLoadProfileSpec().getInterval() : interval);
    }

    public boolean isUseMultiplier() {
        return useMultiplier;
    }

    public void setUseMultiplier(boolean useMultiplier) {
        this.useMultiplier = useMultiplier;
    }

    public Optional<ReadingType> getCalculatedReadingType() {
        return calculatedReadingType.getOptional();
    }

    public void setCalculatedReadingType(ReadingType calculatedReadingType) {
        this.calculatedReadingType.set(calculatedReadingType);
    }

    @Override
    public void save() {
        validate();
        super.save();
        if (this.loadProfileSpec.isPresent()) {
            getDataModel().touch(this.loadProfileSpec.get());
        }
    }

    @Override
    public List<ValidationRule> getValidationRules() {
        Set<ReadingType> result = new HashSet<>();
        ReadingType readingType = getReadingType();
        result.add(readingType);
        if (readingType.isCumulative()) {
            readingType.getCalculatedReadingType().ifPresent(result::add);
        }
        return getDeviceConfiguration().getValidationRules(result);
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public ReadingType getReadingType() {
        return getChannelType().getReadingType();
    }

    private void validate() {
        validateInterval();
        validateDeviceTypeContainsChannelType();
        validateChannelSpecsForDuplicateChannelTypes();
        validateDeviceConfigurationContainsLoadProfileSpec();
    }

    private void validateBeforeAdd() {
        Save.CREATE.validate(this.getDataModel(), this);
    }

    private void validateInterval() {
        if (!this.loadProfileSpec.isPresent()) {
            if (getInterval() == null) {
                throw IntervalIsRequiredException.forChannelSpecWithoutLoadProfileSpec(this.getThesaurus(), MessageSeeds.CHANNEL_SPEC_WITHOUT_LOAD_PROFILE_SPEC_INTERVAL_IS_REQUIRED);
            }
            if (getInterval().getCount() <= 0) {
                throw UnsupportedIntervalException.intervalOfChannelSpecShouldBeLargerThanZero(getInterval().getCount(), this.getThesaurus(), MessageSeeds.CHANNEL_SPEC_INVALID_INTERVAL_COUNT);
            }
            if ((getInterval().getTimeUnit() == TimeDuration.TimeUnit.DAYS ||
                    getInterval().getTimeUnit() == TimeDuration.TimeUnit.MONTHS ||
                    getInterval().getTimeUnit() == TimeDuration.TimeUnit.YEARS) &&
                    getInterval().getCount() != 1) {
                throw UnsupportedIntervalException.intervalOfChannelShouldBeOneIfUnitIsLargerThanOneHour(getInterval().getCount(), this.getThesaurus(), MessageSeeds.CHANNEL_SPEC_INVALID_INTERVAL_COUNT_LARGE_UNIT);
            }
            if (getInterval().getTimeUnit() == TimeDuration.TimeUnit.WEEKS) {
                throw UnsupportedIntervalException.weeksAreNotSupportedForChannelSpecs(this.getThesaurus(), this, MessageSeeds.CHANNEL_SPEC_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED);
            }
        }
    }

    private void validateChannelSpecsForDuplicateChannelTypes() {
        List<String> readingTypesInUseByChannelType = getChannelReadingTypes(this);
        for (ChannelSpec channelSpec : getDeviceConfiguration().getChannelSpecs()) {
            if (!isSameIdObject(this, channelSpec)) {
                Optional<ReadingType> duplicateReadingType = findDuplicateReadingType(channelSpec, readingTypesInUseByChannelType);
                if(duplicateReadingType.isPresent()){
                    throw DuplicateChannelTypeException.duplicateChannelSpecOnDeviceConfiguration(channelSpec, getChannelType(), channelSpec.getLoadProfileSpec(), this.getThesaurus(), MessageSeeds.CHANNEL_SPEC_DUPLICATE_CHANNEL_TYPE_IN_LOAD_PROFILE_SPEC, duplicateReadingType.get());
                }
            }
        }
    }

    private List<String> getChannelReadingTypes(ChannelSpec channelSpec) {
        List<String> readingTypesInUseByChannelType = new ArrayList<>(2);
        readingTypesInUseByChannelType.add(channelSpec.getChannelType().getReadingType().getMRID());
        getCalculatedOrOverriddenReadingType(channelSpec).map(ReadingType::getMRID).ifPresent(readingTypesInUseByChannelType::add);
        return readingTypesInUseByChannelType;
    }

    private Optional<ReadingType> getCalculatedOrOverriddenReadingType(ChannelSpec channelSpec) {
        if (channelSpec.isUseMultiplier()) {
            return channelSpec.getCalculatedReadingType();
        }
        return channelSpec.getChannelType().getReadingType().getCalculatedReadingType();
    }

    private Optional<ReadingType> findDuplicateReadingType(ChannelSpec candidate, Collection<String> readingTypeMrids) {
        Objects.requireNonNull(candidate);
        Objects.requireNonNull(readingTypeMrids);
        if (!readingTypeMrids.contains(candidate.getReadingType().getMRID())) {
            Optional<ReadingType> calculatedReadingType = getCalculatedOrOverriddenReadingType(candidate);
            if(calculatedReadingType.isPresent() && readingTypeMrids.contains(calculatedReadingType.get().getMRID())){
                return calculatedReadingType;
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(candidate.getReadingType());
        }
    }

    private void validateDeviceTypeContainsChannelType() {
        if (this.loadProfileSpec.isPresent()) { // then the ChannelType should be included in the LoadProfileSpec
            if (!doesListContainIdObject(getLoadProfileSpec().getLoadProfileType().getChannelTypes(), getChannelType())) {
                throw RegisterTypeIsNotConfiguredException.forChannelInLoadProfileSpec(getLoadProfileSpec(), getChannelType(), this, this.getThesaurus(), MessageSeeds.CHANNEL_SPEC_CHANNEL_TYPE_IS_NOT_IN_LOAD_PROFILE_SPEC);
            }
        } else { // then the ChannelType should be included in the DeviceType
            if (!doesListContainIdObject(getDeviceConfiguration().getDeviceType().getRegisterTypes(), getChannelType())) {
                throw RegisterTypeIsNotConfiguredException.forChannelInDeviceType(this, getChannelType(), getDeviceConfiguration().getDeviceType(), this.getThesaurus(), MessageSeeds.CHANNEL_SPEC_CHANNEL_TYPE_IS_NOT_ON_DEVICE_TYPE);
            }
        }
    }

    private void validateDeviceConfigurationContainsLoadProfileSpec() {
        if (this.loadProfileSpec.isPresent()) {
            if (!doesListContainIdObject(getDeviceConfiguration().getLoadProfileSpecs(), getLoadProfileSpec())) {
                throw new LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException(getLoadProfileSpec(), this.getThesaurus(), MessageSeeds.CHANNEL_SPEC_LOAD_PROFILE_SPEC_IS_NOT_ON_DEVICE_CONFIGURATION);
            }
        }
    }

    @Override
    public void configurationBeingDeleted() {
        this.getDataModel().remove(this);
    }

    @Override
    protected void doDelete() {
        getDeviceConfiguration().removeChannelSpec(this);
    }

    private void validateUpdate() {
        Save.UPDATE.validate(this.getDataModel(), this);
        this.validate();
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.CHANNELSPEC;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.CHANNELSPEC;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.CHANNELSPEC;
    }

    @Override
    public void validateDelete() {
        // the configuration will validate the active 'part'
    }

    @Override
    public String toString() {
        return getDeviceConfiguration().getDeviceType().getName() + "/" + getDeviceConfiguration().getName() + "/" + getReadingType().getAliasName();
    }

    public void setChannelType(ChannelType channelType) {
        if (this.channelType.isPresent()) {
            validateChannelTypeForUpdate(channelType);
        }
        this.channelType.set(channelType);
    }

    private void validateChannelTypeForUpdate(MeasurementType measurementType) {
        DeviceConfiguration deviceConfiguration = getDeviceConfiguration();
        MeasurementType myMeasurementType = this.getChannelType();
        if (deviceConfiguration != null && deviceConfiguration.isActive() && myMeasurementType != null && myMeasurementType.getId() != measurementType.getId()) {
            throw new CannotChangeChannelTypeOfChannelSpecException(this.getThesaurus(), MessageSeeds.CHANNEL_SPEC_CANNOT_CHANGE_CHANNEL_TYPE);
        }
    }


    public void setOverruledObisCode(ObisCode overruledObisCode) {
        if (overruledObisCode != null) {
            this.overruledObisCodeString = overruledObisCode.toString();
        }
        this.overruledObisCode = overruledObisCode;
    }

    public void setNbrOfFractionDigits(int nbrOfFractionDigits) {
        this.nbrOfFractionDigits = nbrOfFractionDigits;
    }

    public void setOverflow(BigDecimal overflow) {
        this.overflow = overflow;
    }

    public void setLoadProfileSpec(LoadProfileSpecImpl loadProfileSpec) {
        validateLoadProfileSpecForUpdate(loadProfileSpec);
        this.loadProfileSpec.set(loadProfileSpec);
        setInterval(getLoadProfileSpec().getInterval());    // if the channel is linked to a LoadProfileSpec, then the interval must be the same as that of the LoadProfileType
    }

    private void validateLoadProfileSpecForUpdate(LoadProfileSpec loadProfileSpec) {
        if (deviceConfiguration.isPresent() && getDeviceConfiguration().isActive() && this.loadProfileSpec.isPresent() && this.getLoadProfileSpec().getId() != loadProfileSpec.getId()) {
            throw new CannotChangeLoadProfileSpecOfChannelSpec(this.getThesaurus(), MessageSeeds.CHANNEL_SPEC_CANNOT_CHANGE_LOAD_PROFILE_SPEC);
        }
    }

    public void setInterval(TimeDuration interval) {
        this.interval = interval;
    }

    abstract static class ChannelSpecBuilder implements ChannelSpec.ChannelSpecBuilder, BuildingCompletionListener {

        LoadProfileSpecImpl loadProfileSpec;
        final ChannelSpecImpl channelSpec;

        ChannelSpecBuilder(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, ChannelType channelType, LoadProfileSpecImpl loadProfileSpec) {
            this.loadProfileSpec = loadProfileSpec;
            this.channelSpec = channelSpecProvider.get().initialize(deviceConfiguration, channelType, loadProfileSpec);
        }

        ChannelSpecBuilder(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, ChannelType channelType, ServerLoadProfileSpecBuilder loadProfileSpecBuilder) {
            this.channelSpec = channelSpecProvider.get().initialize(deviceConfiguration, channelType);
            loadProfileSpecBuilder.notifyOnAdd(this);
        }

        @Override
        public void loadProfileSpecBuildingProcessCompleted(LoadProfileSpec loadProfileSpec) {
            this.loadProfileSpec = (LoadProfileSpecImpl) loadProfileSpec;
            this.channelSpec.setLoadProfileSpec(this.loadProfileSpec);
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder overruledObisCode(ObisCode overruledObisCode) {
            this.channelSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder nbrOfFractionDigits(int nbrOfFractionDigits) {
            this.channelSpec.setNbrOfFractionDigits(nbrOfFractionDigits);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder overflow(BigDecimal overflow) {
            this.channelSpec.setOverflow(overflow);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder interval(TimeDuration interval) {
            this.channelSpec.setInterval(interval);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder noMultiplier() {
            this.channelSpec.setUseMultiplier(false);
            this.channelSpec.setCalculatedReadingType(null);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder useMultiplierWithCalculatedReadingType(ReadingType calculatedReadingType) {
            this.channelSpec.setUseMultiplier(true);
            this.channelSpec.setCalculatedReadingType(calculatedReadingType);
            return this;
        }

        @Override
        public ChannelSpec add() {
            this.channelSpec.validateBeforeAdd();
            this.channelSpec.validate();
            this.loadProfileSpec.addChannelSpec(this.channelSpec);
            return this.channelSpec;
        }
    }

    abstract static class ChannelSpecUpdater implements ChannelSpec.ChannelSpecUpdater {

        final ChannelSpecImpl channelSpec;

        protected ChannelSpecUpdater(ChannelSpecImpl channelSpec) {
            this.channelSpec = channelSpec;
        }

        @Override
        public ChannelSpec.ChannelSpecUpdater overruledObisCode(ObisCode overruledObisCode) {
            this.channelSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecUpdater nbrOfFractionDigits(int nbrOfFractionDigits) {
            this.channelSpec.setNbrOfFractionDigits(nbrOfFractionDigits);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecUpdater overflow(BigDecimal overflow) {
            this.channelSpec.setOverflow(overflow);
            return this;
        }


        @Override
        public ChannelSpec.ChannelSpecUpdater noMultiplier() {
            this.channelSpec.setUseMultiplier(false);
            this.channelSpec.setCalculatedReadingType(null);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecUpdater useMultiplierWithCalculatedReadingType(ReadingType calculatedReadingType) {
            this.channelSpec.setUseMultiplier(true);
            this.channelSpec.setCalculatedReadingType(calculatedReadingType);
            return this;
        }

        @Override
        public void update() {
            this.channelSpec.validateUpdate();
            this.channelSpec.save();
        }
    }

}
