package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotChangeDeviceConfigurationReferenceException;
import com.energyict.mdc.device.config.exceptions.CannotChangeLoadProfileSpecOfChannelSpec;
import com.energyict.mdc.device.config.exceptions.CannotChangeRegisterMappingOfChannelSpecException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigIsRequiredException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.DuplicateRegisterMappingException;
import com.energyict.mdc.device.config.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.device.config.exceptions.LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.MultiplierIsRequiredException;
import com.energyict.mdc.device.config.exceptions.MultiplierModeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.PhenomenonIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ReadingMethodIsRequiredException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingIsNotConfiguredException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingIsRequiredException;
import com.energyict.mdc.device.config.exceptions.UnitsNotCompatibleException;
import com.energyict.mdc.device.config.exceptions.UnsupportedIntervalException;
import com.energyict.mdc.device.config.exceptions.ValueCalculationMethodIsRequiredException;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:22
 */
public class ChannelSpecImpl extends PersistentNamedObject<ChannelSpec> implements ChannelSpec {

    private final DeviceConfigurationService deviceConfigurationService;

    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private final Reference<RegisterMapping> registerMapping = ValueReference.absent();
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private int nbrOfFractionDigits;
    private BigDecimal overflow;
    private final Reference<Phenomenon> phenomenon = ValueReference.absent();
    private ReadingMethod readingMethod;
    private MultiplierMode multiplierMode;
    private BigDecimal multiplier;
    private ValueCalculationMethod valueCalculationMethod;
    private final Reference<LoadProfileSpec> loadProfileSpec = ValueReference.absent();
    private TimeDuration interval;
    private final Reference<ProductSpec> productSpec = ValueReference.absent();

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
        setDeviceConfiguration(deviceConfiguration);
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
        if (!Checks.is(this.overruledObisCodeString).empty()) {
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
        return (getLoadProfileSpec() != null ? getLoadProfileSpec().getInterval() : interval);
    }

    @Override
    public void save() {
        validateRequiredFields();
        super.save();
    }

    private void validateRequiredFields() {
        validateDeviceConfiguration();
        validateRegisterMapping();
        validateDeviceTypeContainsRegisterMapping();
        validateChannelSpecsForDuplicateRegisterMappings();
        validateDeviceConfigurationContainsLoadProfileSpec();
        validatePhenomenon();
        validatePhenomenonAndRegisterMappingUnitCompatibility();
        validateReadingMethod();
        validateMultiplierMode();
        validateValueCalculationMethod();
        validateInterval();
        validateForCollectionMethod();
    }

    private void validateForCollectionMethod() {
        // TODO Check if this is still required as CollectionMethod is not on DeviceType anymore ...

//        if (DeviceCollectionMethodType.COMSERVER.equals(getDeviceConfig().getDeviceType().getC)) {
//            if (spec == null) {
//                throw new BusinessException("loadProfileSpecForChannelXCannotBeNull",
//                        "Channel '{0}' cannot have an undefined load profile specification when the collection method of its device type = ComServer", name);
//            }
//        }
    }

    private void validateInterval() {
        if (getLoadProfileSpec() == null) {
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
        if (channelSpec != null) {
            throw DuplicateRegisterMappingException.forChannelSpecInLoadProfileSpec(thesaurus, channelSpec, getRegisterMapping(), this.getLoadProfileSpec());
        }
    }

    private void validateValueCalculationMethod() {
        if (this.valueCalculationMethod == null) {
            throw ValueCalculationMethodIsRequiredException.forChannelSpec(this.thesaurus, this);
        }
    }

    private void validateMultiplierMode() {
        if (this.multiplierMode == null) {
            throw MultiplierModeIsRequiredException.forChannelSpec(this.thesaurus, this);
        } else {
            if (MultiplierMode.CONFIGURED_ON_OBJECT.equals(this.multiplierMode) && getMultiplier() == null) {
                throw MultiplierIsRequiredException.onChannelSpecWhenModeIsOnObject(thesaurus, this, this.multiplierMode);
            }
        }
    }

    private void validateReadingMethod() {
        if (readingMethod == null) {
            throw ReadingMethodIsRequiredException.forChannelSpec(this.thesaurus, this);
        }

    }

    private void validatePhenomenonAndRegisterMappingUnitCompatibility() {
        Unit registerMappingUnit = getRegisterMapping().getUnit();
        Phenomenon phenomenon = this.getPhenomenon();
        if (!phenomenon.isUndefined() && !registerMappingUnit.isUndefined()) {
            if (!phenomenon.getUnit().equalBaseUnit(registerMappingUnit)) {
                throw UnitsNotCompatibleException.forChannelSpecPhenomenonAndRegisterMappingUnit(thesaurus, phenomenon, registerMappingUnit);
            }
        }
    }

    private void validateDeviceConfiguration() {
        if (this.deviceConfiguration == null) {
            throw DeviceConfigIsRequiredException.channelSpecRequiresDeviceConfig(this.thesaurus);
        }
    }

    private void validateRegisterMapping() {
        if (this.registerMapping == null) {
            throw RegisterMappingIsRequiredException.channelSpecRequiresRegisterMapping(this.thesaurus);
        }
    }

    private void validateDeviceTypeContainsRegisterMapping() {
        if (getLoadProfileSpec() != null) { // then the RegisterMapping should be included in the LoadProfileSpec
            if (!getLoadProfileSpec().getLoadProfileType().getRegisterMappings().contains(getRegisterMapping())) {
                throw RegisterMappingIsNotConfiguredException.forChannelInLoadProfileSpec(thesaurus, getLoadProfileSpec(), getRegisterMapping(), this);
            }
        } else { // then the RegisterMapping should be included in the DeviceType
            if (!getDeviceConfiguration().getDeviceType().getRegisterMappings().contains(getRegisterMapping())) {
                throw RegisterMappingIsNotConfiguredException.forChannelInDeviceType(thesaurus, this, getRegisterMapping(), getDeviceConfiguration().getDeviceType());
            }
        }
    }

    private void validateActiveConfig() {
        if (getDeviceConfiguration().getActive()) {
            throw CannotAddToActiveDeviceConfigurationException.aNewChannelSpec(this.thesaurus);
        }
    }

    private void validateDeviceConfigurationContainsLoadProfileSpec() {
        if (getLoadProfileSpec() != null) {
            if (!getDeviceConfiguration().getLoadProfileSpecs().contains(getLoadProfileSpec())) {
                throw new LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException(this.thesaurus, getLoadProfileSpec());
            }
        }
    }

    private void validatePhenomenon() {
        if (phenomenon == null) {
            throw PhenomenonIsRequiredException.forChannelSpec(thesaurus, this);
        }
    }

    @Override
    protected void postNew() {
        validateActiveConfig();
        this.getDataMapper().persist(this);
    }

    @Override
    protected void post() {
        this.getDataMapper().update(this);
    }

    @Override
    protected void doDelete() {
        getDeviceConfiguration().deleteChannelSpec(this);
    }

    @Override
    public void delete() {
        getDeviceConfiguration().deleteChannelSpec(this);
    }

    @Override
    public void validateDelete() {
        // the configuration will validate the active 'part'
    }

    protected void validateUniqueName(String name) {
        if (this.deviceConfigurationService.findChannelSpecByDeviceConfigurationAndName(getDeviceConfiguration(), name) != null) {
            throw DuplicateNameException.channelSpecAlreadyExists(this.getThesaurus(), name);
        }
        if (name.length() > 27) {
            if (this.deviceConfigurationService.findChannelSpecByDeviceConfigurationAndName(getDeviceConfiguration(), name.substring(0, 27)) != null) {
                throw DuplicateNameException.channelSpecAlreadyExistsFirstChars(this.getThesaurus(), name.substring(0, 27));
            }
        }
    }


    @Override
    public String toString() {
        return getDeviceConfiguration().getDeviceType() + "/" + getDeviceConfiguration() + "/" + getName();
    }

    protected String getInvalidCharacters() {
        return "./";
    }

    @Override
    protected NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus) {
        return NameIsRequiredException.channelSpecNameIsRequired(thesaurus);
    }

    @Override
    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        validateDeviceConfigurationForUpdate(deviceConfiguration);
        this.deviceConfiguration.set(deviceConfiguration);
    }

    private void validateDeviceConfigurationForUpdate(DeviceConfiguration deviceConfiguration) {
        DeviceConfiguration myDeviceConfiguration = this.getDeviceConfiguration();
        if (myDeviceConfiguration != null && myDeviceConfiguration.getId() != deviceConfiguration.getId()) {
            throw CannotChangeDeviceConfigurationReferenceException.forChannelSpec(this.thesaurus, this);
        }
    }

    @Override
    public void setRegisterMapping(RegisterMapping registerMapping) {
        validateRegisterMappingForUpdate(registerMapping);
        this.registerMapping.set(registerMapping);
    }

    private void validateRegisterMappingForUpdate(RegisterMapping registerMapping) {
        DeviceConfiguration deviceConfiguration = getDeviceConfiguration();
        RegisterMapping myRegisterMapping = this.getRegisterMapping();
        if (deviceConfiguration != null && deviceConfiguration.getActive() && myRegisterMapping != null && myRegisterMapping.getId() != registerMapping.getId()) {
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
    }

    @Override
    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public void setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod) {
        this.valueCalculationMethod = valueCalculationMethod;
    }

    @Override
    public void setLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        validateLoadProfileSpecForUpdate(loadProfileSpec);
        this.loadProfileSpec.set(loadProfileSpec);
    }

    private void validateLoadProfileSpecForUpdate(LoadProfileSpec loadProfileSpec) {
        DeviceConfiguration deviceConfiguration = getDeviceConfiguration();
        if (deviceConfiguration != null && deviceConfiguration.getActive() && this.getLoadProfileSpec().getId() != loadProfileSpec.getId()) {
            throw new CannotChangeLoadProfileSpecOfChannelSpec(this.thesaurus);
        }
    }

    @Override
    public void setInterval(TimeDuration interval) {
        this.interval = interval;
    }

    @Override
    public void setProductSpec(ProductSpec productSpec) {
        this.productSpec.set(productSpec);
    }

    @Override
    public ProductSpec getProductSpec() {
        return productSpec.get();
    }

    public static class ChannelSpecBuilder implements ChannelSpec.ChannelSpecBuilder {

        final ChannelSpecImpl channelSpec;

        public ChannelSpecBuilder(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
            this.channelSpec = channelSpecProvider.get().initialize(deviceConfiguration, registerMapping, phenomenon, loadProfileSpec);
        }

        public ChannelSpecBuilder(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
            this.channelSpec = channelSpecProvider.get().initialize(deviceConfiguration, registerMapping, phenomenon);
            loadProfileSpecBuilder.notifyOnAdd(this);
        }

        @Override
        public void loadProfileSpecBuildingProcessCompleted(LoadProfileSpec loadProfileSpec) {
            this.channelSpec.setLoadProfileSpec(loadProfileSpec);
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
        public ChannelSpec.ChannelSpecBuilder setPhenomenon(Phenomenon phenomenon) {
            this.channelSpec.setPhenomenon(phenomenon);
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
        public ChannelSpec.ChannelSpecBuilder setProductSpec(ProductSpec productSpec) {
            this.channelSpec.setProductSpec(productSpec);
            return this;
        }

        @Override
        public ChannelSpec add(){
            this.channelSpec.validateRequiredFields();
            return this.channelSpec;
        }
    }

}
