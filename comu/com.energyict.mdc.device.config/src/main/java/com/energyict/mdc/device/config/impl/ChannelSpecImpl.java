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
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.RegisterTypeIsNotConfiguredException;
import com.energyict.mdc.device.config.exceptions.UnsupportedIntervalException;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;

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

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:22
 */
public class ChannelSpecImpl extends PersistentIdObject<ChannelSpec> implements ChannelSpec {

    private final ServerDeviceConfigurationService deviceConfigurationService;

    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CHANNEL_SPEC_CHANNEL_TYPE_IS_REQUIRED + "}")
    private final Reference<ChannelType> channelType = ValueReference.absent();
    private final Reference<LoadProfileSpecImpl> loadProfileSpec = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CHANNEL_SPEC_READING_METHOD_IS_REQUIRED + "}")
    private ReadingMethod readingMethod = ReadingMethod.ENGINEERING_UNIT;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CHANNEL_SPEC_VALUE_CALCULATION_METHOD_IS_REQUIRED + "}")
    private ValueCalculationMethod valueCalculationMethod = ValueCalculationMethod.AUTOMATIC;
    private int nbrOfFractionDigits = 0;
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

    @Inject
    public ChannelSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ServerDeviceConfigurationService deviceConfigurationService) {
        super(ChannelSpec.class, dataModel, eventService, thesaurus);
        this.deviceConfigurationService = deviceConfigurationService;
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
    public BigDecimal getOverflow() {
        return overflow;
    }

    @Override
    public ReadingMethod getReadingMethod() {
        return readingMethod;
    }

    @Override
    public ValueCalculationMethod getValueCalculationMethod() {
        return valueCalculationMethod;
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

    @Override
    public void save() {
        validate();
        super.save();
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
                throw IntervalIsRequiredException.forChannelSpecWithoutLoadProfileSpec(this.getThesaurus());
            }
            if (getInterval().getCount() <= 0) {
                throw UnsupportedIntervalException.intervalOfChannelSpecShouldBeLargerThanZero(this.getThesaurus(), getInterval().getCount());
            }
            if ((getInterval().getTimeUnit() == TimeDuration.TimeUnit.DAYS ||
                    getInterval().getTimeUnit() == TimeDuration.TimeUnit.MONTHS ||
                    getInterval().getTimeUnit() == TimeDuration.TimeUnit.YEARS) &&
                    getInterval().getCount() != 1) {
                throw UnsupportedIntervalException.intervalOfChannelShouldBeOneIfUnitIsLargerThanOneHour(this.getThesaurus(), getInterval().getCount());
            }
            if (getInterval().getTimeUnit() == TimeDuration.TimeUnit.WEEKS) {
                throw UnsupportedIntervalException.weeksAreNotSupportedForChannelSpecs(this.getThesaurus(), this);
            }
        }
    }

    private void validateChannelSpecsForDuplicateChannelTypes() {
        List<String> readingTypesInUseByChannelType = new ArrayList<>(2);
        readingTypesInUseByChannelType.add(getChannelType().getReadingType().getMRID());
        getChannelType().getReadingType().getCalculatedReadingType().ifPresent(calculatedReadingType -> {
            readingTypesInUseByChannelType.add(calculatedReadingType.getMRID());
        });
        for (ChannelSpec channelSpec : getLoadProfileSpec().getChannelSpecs()) {
            if (!isSameIdObject(this, channelSpec)
                    && !readingTypesAreNotUsedByChannelType(channelSpec.getChannelType(), readingTypesInUseByChannelType)){
                throw DuplicateChannelTypeException.forChannelSpecInLoadProfileSpec(this.getThesaurus(), channelSpec, getChannelType(), this.getLoadProfileSpec());
            }
        }
    }

    private boolean readingTypesAreNotUsedByChannelType(ChannelType candidate, Collection<String> readingTypeMrids){
        Objects.requireNonNull(candidate);
        Objects.requireNonNull(readingTypeMrids);
        if (!readingTypeMrids.contains(candidate.getReadingType().getMRID())){
            Optional<ReadingType> calculatedReadingType = candidate.getReadingType().getCalculatedReadingType();
            return !calculatedReadingType.isPresent() || !readingTypeMrids.contains(calculatedReadingType.get().getMRID());
        }
        return false;
    }

    private void validateDeviceTypeContainsChannelType() {
        if (this.loadProfileSpec.isPresent()) { // then the ChannelType should be included in the LoadProfileSpec
            if (!doesListContainIdObject(getLoadProfileSpec().getLoadProfileType().getChannelTypes(), getChannelType())) {
                throw RegisterTypeIsNotConfiguredException.forChannelInLoadProfileSpec(this.getThesaurus(), getLoadProfileSpec(), getChannelType(), this);
            }
        } else { // then the ChannelType should be included in the DeviceType
            if (!doesListContainIdObject(getDeviceConfiguration().getDeviceType().getRegisterTypes(), getChannelType())) {
                throw RegisterTypeIsNotConfiguredException.forChannelInDeviceType(this.getThesaurus(), this, getChannelType(), getDeviceConfiguration().getDeviceType());
            }
        }
    }

    private void validateDeviceConfigurationContainsLoadProfileSpec() {
        if (this.loadProfileSpec.isPresent()) {
            if (!doesListContainIdObject(getDeviceConfiguration().getLoadProfileSpecs(), getLoadProfileSpec())) {
                throw new LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException(this.getThesaurus(), getLoadProfileSpec());
            }
        }
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

    @Override
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
            throw new CannotChangeChannelTypeOfChannelSpecException(this.getThesaurus());
        }
    }


    @Override
    public void setOverruledObisCode(ObisCode overruledObisCode) {
        if (overruledObisCode != null) {
            this.overruledObisCodeString = overruledObisCode.toString();
        }
        this.overruledObisCode = overruledObisCode;
    }

    @Override
    public void setNbrOfFractionDigits(int nbrOfFractionDigits) {
        this.nbrOfFractionDigits = nbrOfFractionDigits;
    }

    @Override
    public void setOverflow(BigDecimal overflow) {
        this.overflow = overflow;
    }

    @Override
    public void setReadingMethod(ReadingMethod readingMethod) {
        this.readingMethod = readingMethod;
    }

    @Override
    public void setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod) {
        this.valueCalculationMethod = valueCalculationMethod;
    }

    public void setLoadProfileSpec(LoadProfileSpecImpl loadProfileSpec) {
        validateLoadProfileSpecForUpdate(loadProfileSpec);
        this.loadProfileSpec.set(loadProfileSpec);
        setInterval(getLoadProfileSpec().getInterval());    // if the channel is linked to a LoadProfileSpec, then the interval must be the same as that of the LoadProfileType
    }

    private void validateLoadProfileSpecForUpdate(LoadProfileSpec loadProfileSpec) {
        if (deviceConfiguration.isPresent() && getDeviceConfiguration().isActive() && this.loadProfileSpec.isPresent() && this.getLoadProfileSpec().getId() != loadProfileSpec.getId()) {
            throw new CannotChangeLoadProfileSpecOfChannelSpec(this.getThesaurus());
        }
    }

    @Override
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
        public ChannelSpec.ChannelSpecBuilder setOverruledObisCode(ObisCode overruledObisCode) {
            this.channelSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder setNbrOfFractionDigits(int nbrOfFractionDigits) {
            this.channelSpec.setNbrOfFractionDigits(nbrOfFractionDigits);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder setOverflow(BigDecimal overflow) {
            this.channelSpec.setOverflow(overflow);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder setReadingMethod(ReadingMethod readingMethod) {
            this.channelSpec.setReadingMethod(readingMethod);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod) {
            this.channelSpec.setValueCalculationMethod(valueCalculationMethod);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder setInterval(TimeDuration interval) {
            this.channelSpec.setInterval(interval);
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
        public ChannelSpec.ChannelSpecUpdater setOverruledObisCode(ObisCode overruledObisCode) {
            this.channelSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecUpdater setNbrOfFractionDigits(int nbrOfFractionDigits) {
            this.channelSpec.setNbrOfFractionDigits(nbrOfFractionDigits);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecUpdater setOverflow(BigDecimal overflow) {
            this.channelSpec.setOverflow(overflow);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecUpdater setReadingMethod(ReadingMethod readingMethod) {
            this.channelSpec.setReadingMethod(readingMethod);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecUpdater setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod) {
            this.channelSpec.setValueCalculationMethod(valueCalculationMethod);
            return this;
        }

        @Override
        public void update() {
            this.channelSpec.validateUpdate();
            this.channelSpec.save();
        }
    }

}
