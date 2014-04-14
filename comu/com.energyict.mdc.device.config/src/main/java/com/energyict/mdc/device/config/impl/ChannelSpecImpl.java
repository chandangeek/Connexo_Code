package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotChangeLoadProfileSpecOfChannelSpec;
import com.energyict.mdc.device.config.exceptions.CannotChangeRegisterMappingOfChannelSpecException;
import com.energyict.mdc.device.config.exceptions.DuplicateRegisterMappingException;
import com.energyict.mdc.device.config.exceptions.IncompatibleUnitsException;
import com.energyict.mdc.device.config.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.device.config.exceptions.LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.RegisterMappingIsNotConfiguredException;
import com.energyict.mdc.device.config.exceptions.UnsupportedIntervalException;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static com.elster.jupiter.util.Checks.is;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:22
 */
public class ChannelSpecImpl extends PersistentNamedObject<ChannelSpec> implements ChannelSpec {

    private final DeviceConfigurationService deviceConfigurationService;

    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.CHANNEL_SPEC_REGISTER_MAPPING_IS_REQUIRED_KEY + "}")
    private final Reference<RegisterMapping> registerMapping = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.CHANNEL_SPEC_PHENOMENON_IS_REQUIRED_KEY + "}")
    private final Reference<Phenomenon> phenomenon = ValueReference.absent();
    private final Reference<LoadProfileSpec> loadProfileSpec = ValueReference.absent();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.CHANNEL_SPEC_READING_METHOD_IS_REQUIRED_KEY + "}")
    private ReadingMethod readingMethod = ReadingMethod.ENGINEERING_UNIT;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.CHANNEL_SPEC_VALUE_CALCULATION_METHOD_IS_REQUIRED_KEY + "}")
    private ValueCalculationMethod valueCalculationMethod = ValueCalculationMethod.AUTOMATIC;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.CHANNEL_SPEC_MULTIPLIER_MODE_IS_REQUIRED_KEY + "}")
    private MultiplierMode multiplierMode = MultiplierMode.CONFIGURED_ON_OBJECT;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.CHANNEL_SPEC_MULTIPLIER_IS_REQUIRED_WHEN_KEY + "}")
    private BigDecimal multiplier = BigDecimal.ONE;
    private int nbrOfFractionDigits = 0;
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private BigDecimal overflow;
    private TimeDuration interval;

    @Inject
    public ChannelSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, DeviceConfigurationService deviceConfigurationService) {
        super(ChannelSpec.class, dataModel, eventService, thesaurus);
        this.deviceConfigurationService = deviceConfigurationService;
    }

    private ChannelSpecImpl initialize(DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
        this.initialize(deviceConfiguration, registerMapping, phenomenon);
        setLoadProfileSpec(loadProfileSpec);
        return this;
    }

    private ChannelSpecImpl initialize(DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping, Phenomenon phenomenon) {
        this.deviceConfiguration.set(deviceConfiguration);
        setRegisterMapping(registerMapping);
        setPhenomenon(phenomenon);
        return this;
    }

    @Override
    public RegisterMapping getRegisterMapping() {
        return registerMapping.get();
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
        return getRegisterMapping().getObisCode();
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
    public Phenomenon getPhenomenon() {
        return this.phenomenon.get();
    }

    @Override
    public ReadingMethod getReadingMethod() {
        return readingMethod;
    }

    @Override
    public MultiplierMode getMultiplierMode() {
        return multiplierMode;
    }

    @Override
    public BigDecimal getMultiplier() {
        return multiplier;
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
        this.applyDefaultMultiplierForConfiguredOnObjectMode();
        validate();
        super.save();
    }

    private void applyDefaultMultiplierForConfiguredOnObjectMode() {
        if (!this.multiplierMode.equals(MultiplierMode.CONFIGURED_ON_OBJECT)) {
            this.multiplier = BigDecimal.ONE;
        }
    }

    private void validate() {
        validateInterval();
        validateDeviceTypeContainsRegisterMapping();
        validateChannelSpecsForDuplicateRegisterMappings();
        validateDeviceConfigurationContainsLoadProfileSpec();
        validatePhenomenonAndRegisterMappingUnitCompatibility();
    }

    private void validateBeforeAdd () {
        Save.CREATE.validate(this.dataModel.getValidatorFactory().getValidator(), this);
    }

    private void validateInterval() {
        if (!this.loadProfileSpec.isPresent()) {
            if (getInterval() == null) {
                throw IntervalIsRequiredException.forChannelSpecWithoutLoadProfileSpec(this.thesaurus);
            }
            if (getInterval().getCount() <= 0) {
                throw UnsupportedIntervalException.intervalOfChannelSpecShouldBeLargerThanZero(this.thesaurus, getInterval().getCount());
            }
            if ((getInterval().getTimeUnitCode() == TimeDuration.DAYS ||
                    getInterval().getTimeUnitCode() == TimeDuration.MONTHS ||
                    getInterval().getTimeUnitCode() == TimeDuration.YEARS) &&
                    getInterval().getCount() != 1) {
                throw UnsupportedIntervalException.intervalOfChannelShouldBeOneIfUnitIsLargerThanOneHour(this.thesaurus, getInterval().getCount());
            }
            if (getInterval().getTimeUnitCode() == TimeDuration.WEEKS) {
                throw UnsupportedIntervalException.weeksAreNotSupportedForChannelSpecs(this.thesaurus, this);
            }
        }
    }

    private void validateChannelSpecsForDuplicateRegisterMappings() {
        ChannelSpec channelSpec = this.deviceConfigurationService.findChannelSpecForLoadProfileSpecAndRegisterMapping(getLoadProfileSpec(), getRegisterMapping());
        if (channelSpec != null && channelSpec.getId() != getId()) {
            throw DuplicateRegisterMappingException.forChannelSpecInLoadProfileSpec(thesaurus, channelSpec, getRegisterMapping(), this.getLoadProfileSpec());
        }
    }

    private void validatePhenomenonAndRegisterMappingUnitCompatibility() {
        if (this.registerMapping.isPresent() && this.phenomenon.isPresent()) {
            Unit registerMappingUnit = getRegisterMapping().getUnit();
            Phenomenon phenomenon = this.getPhenomenon();
            if (!phenomenon.isUndefined() && !registerMappingUnit.isUndefined()) {
                if (!phenomenon.getUnit().equalBaseUnit(registerMappingUnit)) {
                    throw IncompatibleUnitsException.forChannelSpecPhenomenonAndRegisterMappingUnit(thesaurus, phenomenon, registerMappingUnit);
                }
            }
        }
    }

    private void validateDeviceTypeContainsRegisterMapping() {
        if (this.loadProfileSpec.isPresent()) { // then the RegisterMapping should be included in the LoadProfileSpec
            if (!getLoadProfileSpec().getLoadProfileType().getRegisterMappings().contains(getRegisterMapping())) {
                throw RegisterMappingIsNotConfiguredException.forChannelInLoadProfileSpec(thesaurus, getLoadProfileSpec(), getRegisterMapping(), this);
            }
        } else { // then the RegisterMapping should be included in the DeviceType
            if (!getDeviceConfiguration().getDeviceType().getRegisterMappings().contains(getRegisterMapping())) {
                throw RegisterMappingIsNotConfiguredException.forChannelInDeviceType(thesaurus, this, getRegisterMapping(), getDeviceConfiguration().getDeviceType());
            }
        }
    }

    private void validateDeviceConfigurationContainsLoadProfileSpec() {
        if (this.loadProfileSpec.isPresent()) {
            if (!getDeviceConfiguration().getLoadProfileSpecs().contains(getLoadProfileSpec())) {
                throw new LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException(this.thesaurus, getLoadProfileSpec());
            }
        }
    }

    @Override
    protected void doDelete() {
        getDeviceConfiguration().deleteChannelSpec(this);
    }

    private void validateUpdate() {
        Save.UPDATE.validate(this.dataModel.getValidatorFactory().getValidator(), this);
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

    protected boolean validateUniqueName() {
        return this.validateUniqueName(this.getName())
            && (   this.getName().length() <= 27
                || this.validateUniqueName(this.getName().substring(0, 27)));
    }

    protected boolean validateUniqueName(String name) {
        ServerDeviceConfigurationService deviceConfigurationService = (ServerDeviceConfigurationService) this.deviceConfigurationService;
        ChannelSpec otherChannelSpec = deviceConfigurationService.findChannelSpecByDeviceConfigurationAndName(getDeviceConfiguration(), name);
        return otherChannelSpec == null || otherChannelSpec.getId() == this.getId();
    }

    @Override
    public String toString() {
        return getDeviceConfiguration().getDeviceType() + "/" + getDeviceConfiguration() + "/" + getName();
    }

    protected String getInvalidCharacters() {
        return "./";
    }

    @Override
    public void setRegisterMapping(RegisterMapping registerMapping) {
        if (this.registerMapping.isPresent()) {
            validateRegisterMappingForUpdate(registerMapping);
        }
        this.registerMapping.set(registerMapping);
    }

    private void validateRegisterMappingForUpdate(RegisterMapping registerMapping) {
        DeviceConfiguration deviceConfiguration = getDeviceConfiguration();
        RegisterMapping myRegisterMapping = this.getRegisterMapping();
        if (deviceConfiguration != null && deviceConfiguration.isActive() && myRegisterMapping != null && myRegisterMapping.getId() != registerMapping.getId()) {
            throw new CannotChangeRegisterMappingOfChannelSpecException(this.thesaurus);
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
    public void setPhenomenon(Phenomenon phenomenon) {
        this.phenomenon.set(phenomenon);
    }

    @Override
    public void setReadingMethod(ReadingMethod readingMethod) {
        this.readingMethod = readingMethod;
    }

    @Override
    public void setMultiplierMode(MultiplierMode multiplierMode) {
        this.multiplierMode = multiplierMode;
        if (!is(MultiplierMode.CONFIGURED_ON_OBJECT).equalTo(multiplierMode)) {
            this.multiplier = BigDecimal.ONE;
        }
    }

    @Override
    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
        this.multiplierMode = MultiplierMode.CONFIGURED_ON_OBJECT;
    }

    @Override
    public void setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod) {
        this.valueCalculationMethod = valueCalculationMethod;
    }

    @Override
    public void setLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        validateLoadProfileSpecForUpdate(loadProfileSpec);
        this.loadProfileSpec.set(loadProfileSpec);
        setInterval(getLoadProfileSpec().getInterval());    // if the channel is linked to a LoadProfileSpec, then the interval must be the same as that of the LoadProfileType
    }

    private void validateLoadProfileSpecForUpdate(LoadProfileSpec loadProfileSpec) {
        if (deviceConfiguration.isPresent() && getDeviceConfiguration().isActive() && this.getLoadProfileSpec().getId() != loadProfileSpec.getId()) {
            throw new CannotChangeLoadProfileSpecOfChannelSpec(this.thesaurus);
        }
    }

    @Override
    public void setInterval(TimeDuration interval) {
        this.interval = interval;
    }

    abstract static class ChannelSpecBuilder implements ChannelSpec.ChannelSpecBuilder {

        final ChannelSpecImpl channelSpec;
        String tempName;

        ChannelSpecBuilder(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
            this.channelSpec = channelSpecProvider.get().initialize(deviceConfiguration, registerMapping, phenomenon, loadProfileSpec);
        }

        ChannelSpecBuilder(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
            this.channelSpec = channelSpecProvider.get().initialize(deviceConfiguration, registerMapping, phenomenon);
            loadProfileSpecBuilder.notifyOnAdd(this);
        }

        @Override
        public void loadProfileSpecBuildingProcessCompleted(LoadProfileSpec loadProfileSpec) {
            this.channelSpec.setLoadProfileSpec(loadProfileSpec);
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder setName(String channelSpecName) {
            this.tempName = channelSpecName;
            return this;
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
        public ChannelSpec.ChannelSpecBuilder setMultiplierMode(MultiplierMode multiplierMode) {
            this.channelSpec.setMultiplierMode(multiplierMode);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder setMultiplier(BigDecimal multiplier) {
            this.channelSpec.setMultiplier(multiplier);
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
            if(is(tempName).empty()){
                this.channelSpec.setName(this.channelSpec.getRegisterMapping().getName());
            } else {
                this.channelSpec.setName(tempName);
            }
            this.channelSpec.validateBeforeAdd();
            this.channelSpec.validate();
            return this.channelSpec;
        }
    }

    abstract static class ChannelSpecUpdater implements ChannelSpec.ChannelSpecUpdater{

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
        public ChannelSpec.ChannelSpecUpdater setMultiplierMode(MultiplierMode multiplierMode) {
            this.channelSpec.setMultiplierMode(multiplierMode);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecUpdater setMultiplier(BigDecimal multiplier) {
            this.channelSpec.setMultiplier(multiplier);
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
