/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateChannelTypeException;
import com.energyict.mdc.device.config.exceptions.LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.RegisterTypeIsNotConfiguredException;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_PRIMARY_METERED;
import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.FlowDirection.REVERSE;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.fest.assertions.Assertions.assertThat;

public class ChannelSpecImplTest extends DeviceTypeProvidingPersistenceTest {

    private static final String DEVICE_CONFIGURATION_NAME = ChannelSpecImplTest.class.getName() + "Config";
    private static final String LOAD_PROFILE_TYPE_NAME = ChannelSpecImplTest.class.getSimpleName() + "LoadProfileType";

    private final ObisCode channelTypeObisCode = ObisCode.fromString("1.0.1.8.0.255");
    private final ObisCode loadProfileTypeObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final ObisCode overruledChannelSpecObisCode = ObisCode.fromString("1.0.1.8.2.255");
    private final BigDecimal overflow = BigDecimal.valueOf(999999L);
    private final Integer numberOfFractionDigits = 3;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private TimeDuration interval = TimeDuration.days(1);
    private DeviceConfiguration deviceConfiguration;
    private LoadProfileType loadProfileType;
    private RegisterType registerType;
    private RegisterType registerTypeWhichCanNotBeMultiplied;
    private RegisterType calculatedRegisterType;
    private RegisterType deltaRegisterType;
    private ChannelType channelType;
    private final String activeEnergySecondary = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.BULKQUANTITY).code();
    private final ReadingType readingTypeActiveEnergySecondaryMetered = inMemoryPersistence.getMeteringService().getReadingType(activeEnergySecondary).get();
    private final String activeDailyEnergyPrimaryDelta = ReadingTypeCodeBuilder.of(ELECTRICITY_PRIMARY_METERED).period(MacroPeriod.DAILY).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.DELTADELTA).code();
    private final ReadingType readingTypeActiveDailyEnergyPrimaryMeteredDelta = inMemoryPersistence.getMeteringService().getReadingType(activeDailyEnergyPrimaryDelta).get();

    private final String invalidActiveEnergyPrimary = ReadingTypeCodeBuilder.of(ELECTRICITY_PRIMARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.BULKQUANTITY).code();
    private final ReadingType invalidReadingTypeActiveEnergyPrimaryMetered = inMemoryPersistence.getMeteringService().getReadingType(invalidActiveEnergyPrimary).get();
    private final String deltaReadingTypeCode = ReadingTypeCodeBuilder.of(ELECTRICITY_PRIMARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.DELTADELTA).code();
    private final ReadingType deltaReadingType = inMemoryPersistence.getMeteringService().getReadingType(deltaReadingTypeCode).get();

    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithRegisterTypeAndLoadProfileTypeAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithRegisterTypeAndLoadProfileTypeAndDeviceConfiguration() {
        this.registerType = createOrSetRegisterType(readingTypeActiveEnergySecondaryMetered, channelTypeObisCode);
        this.calculatedRegisterType = createOrSetRegisterType(readingTypeActiveEnergySecondaryMetered.getCalculatedReadingType().get(), channelTypeObisCode);
        this.registerTypeWhichCanNotBeMultiplied = createOrSetRegisterType(invalidReadingTypeActiveEnergyPrimaryMetered, channelTypeObisCode);
        this.deltaRegisterType = createOrSetRegisterType(deltaReadingType, channelTypeObisCode);

        loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME, loadProfileTypeObisCode, interval, Arrays.asList(registerType, calculatedRegisterType, registerTypeWhichCanNotBeMultiplied, deltaRegisterType));
        channelType = loadProfileType.findChannelType(registerType).get();
        loadProfileType.save();

        // Business method
        deviceType.setDescription("For ChannelSpec Test purposes only");
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType);
        deviceType.addRegisterType(calculatedRegisterType);

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
    }

    private LoadProfileSpec createDefaultTestingLoadProfileSpecWithOverruledObisCode() {
        LoadProfileSpec loadProfileSpec;
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = getReloadedDeviceConfiguration().createLoadProfileSpec(this.loadProfileType);
        loadProfileSpecBuilder.setOverruledObisCode(overruledChannelSpecObisCode);
        loadProfileSpec = loadProfileSpecBuilder.add();
        return loadProfileSpec;
    }

    private DeviceConfiguration getReloadedDeviceConfiguration(){
        return inMemoryPersistence.getDeviceConfigurationService()
                .findDeviceConfiguration(this.deviceConfiguration.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload device configuration " + this.deviceConfiguration.getId()));
    }

    @Test
    @Transactional
    public void createChannelSpecTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        assertThat(channelSpec.getDeviceConfiguration().getId()).isEqualTo(getReloadedDeviceConfiguration().getId());
        assertThat(channelSpec.getObisCode()).isEqualTo(channelTypeObisCode);
        assertThat(channelSpec.getDeviceObisCode()).isEqualTo(channelTypeObisCode);
        assertThat(channelSpec.getInterval()).isEqualTo(interval);
        assertThat(channelSpec.getLoadProfileSpec()).isEqualTo(loadProfileSpec);
        assertThat(channelSpec.getChannelType().getTemplateRegister().getId()).isEqualTo(this.registerType.getId());
    }

    private ChannelSpec createDefaultChannelSpec(LoadProfileSpec loadProfileSpec) {
        ChannelSpec channelSpec;
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec).overflow(BigDecimal.valueOf(999999L)).nbrOfFractionDigits(3);
        channelSpec = channelSpecBuilder.add();
        return channelSpec;
    }

    @Test
    @Transactional
    public void createWithNumberOfFractionDigitsTest() {
        ChannelSpec channelSpec;
        int digits = 3;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.nbrOfFractionDigits(digits);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getNbrOfFractionDigits()).isEqualTo(digits);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId ="{"+ MessageSeeds.Keys.CHANNEL_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS +"}")
    public void createWithInvalidFractionDigitsTest() {
        int digits = 9;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.nbrOfFractionDigits(digits);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CHANNEL_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS + "}")
    public void updateWithInvalidFractionDigitsTest() {
        int digits = 9;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec defaultChannelSpec = createDefaultChannelSpec(loadProfileSpec);

        defaultChannelSpec.getDeviceConfiguration().getChannelSpecUpdaterFor(defaultChannelSpec).nbrOfFractionDigits(digits).update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CHANNEL_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED + "}")
    public void decreaseNumberOfFractionDigitsTest() {
        int digits = 1;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec defaultChannelSpec = createDefaultChannelSpec(loadProfileSpec);
        defaultChannelSpec.getDeviceConfiguration().activate();
        getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(defaultChannelSpec).nbrOfFractionDigits(digits).update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void createWithoutOverFlowValueTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
        channelSpecBuilder.overflow(null);
        channelSpecBuilder.nbrOfFractionDigits(numberOfFractionDigits);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void updateWithoutOverFlowValueTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec defaultChannelSpec = createDefaultChannelSpec(loadProfileSpec);

        defaultChannelSpec.getDeviceConfiguration().getChannelSpecUpdaterFor(defaultChannelSpec).overflow(null).update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CHANNEL_SPEC_OVERFLOW_DECREASED + "}")
    public void decreaseOverFlowValueTest() {
        BigDecimal overflow = BigDecimal.valueOf(10);
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec defaultChannelSpec = createDefaultChannelSpec(loadProfileSpec);
        defaultChannelSpec.getDeviceConfiguration().activate();
        getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(defaultChannelSpec).overflow(overflow).update();
    }

    @Test
    @Transactional
    public void updateNumberOfFractionDigitsTest() {
        ChannelSpec channelSpec;
        int digits = 3;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.nbrOfFractionDigits(digits);
        channelSpecUpdater.update();

        assertThat(channelSpec.getNbrOfFractionDigits()).isEqualTo(digits);
    }

    @Test
    @Transactional
    public void createWithOverflowValueTest() {
        ChannelSpec channelSpec;
        BigDecimal overflow = BigDecimal.valueOf(10000000L);
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.overflow(overflow);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getOverflow().get()).isEqualTo(overflow);
    }

    @Test
    @Transactional
    public void updateWithOverflowValueTest() {
        ChannelSpec channelSpec;
        BigDecimal overflow = BigDecimal.valueOf(10000000L);
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.overflow(overflow);
        channelSpecUpdater.update();

        assertThat(channelSpec.getOverflow().get()).isEqualTo(overflow);
    }

    @Test
    @Transactional
    public void noOverFlowForDeltaChannelSpecs() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(loadProfileType.findChannelType(deltaRegisterType).get(), loadProfileSpec);
        channelSpecBuilder.nbrOfFractionDigits(numberOfFractionDigits);
        channelSpecBuilder.overflow(null);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getOverflow().isPresent()).isFalse();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CHANNEL_SPEC_INVALID_OVERFLOW_VALUE+"}", property = "overflow")
    public void invalidOverFlowForDeltaChannelSpecs() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(loadProfileType.findChannelType(deltaRegisterType).get(), loadProfileSpec);
        channelSpecBuilder.nbrOfFractionDigits(numberOfFractionDigits);
        channelSpecBuilder.overflow(BigDecimal.ZERO);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getOverflow().isPresent()).isFalse();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.FIELD_IS_REQUIRED+"}", property = "overflow")
    public void overflowIsRequiredTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
        channelSpecBuilder.nbrOfFractionDigits(numberOfFractionDigits);
        channelSpecBuilder.overflow(null);
        channelSpec = channelSpecBuilder.add();
    }

    @Test
    @Transactional
    public void createWithOverruledObisCodeTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.overruledObisCode(overruledChannelSpecObisCode);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getDeviceObisCode()).isEqualTo(overruledChannelSpecObisCode);
        assertThat(channelSpec.getObisCode()).isEqualTo(channelTypeObisCode);
    }

    @Test
    @Transactional
    public void updateWithOverruledObisCodeTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.overruledObisCode(overruledChannelSpecObisCode);
        channelSpecUpdater.update();

        assertThat(channelSpec.getDeviceObisCode()).isEqualTo(overruledChannelSpecObisCode);
        assertThat(channelSpec.getObisCode()).isEqualTo(channelTypeObisCode);
    }

    @Test(expected = RegisterTypeIsNotConfiguredException.class)
    @Transactional
    public void createWithChannelTypeNotInLoadProfileTypeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.BULKQUANTITY).code();
        ReadingType readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();

        RegisterType registerType1 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType).get();
        LoadProfileType otherLPT = inMemoryPersistence.getMasterDataService().newLoadProfileType("SecondLoadProfileType", ObisCode.fromString("1.1.2.2.3.3"), TimeDuration.days(1), Arrays.asList(registerType1));
        otherLPT.save();
        ChannelType otherChannelType = otherLPT.findChannelType(registerType1).get();
        this.deviceType.addRegisterType(registerType1);
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(otherChannelType, loadProfileSpec).overflow(BigDecimal.valueOf(999999L)).nbrOfFractionDigits(3);
        channelSpecBuilder.add();
    }

    @Test(expected = LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException.class)
    @Transactional
    public void createWithLoadProfileSpecFromOtherConfigTest() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME + "Other");
        DeviceConfiguration otherDeviceConfiguration = deviceConfigurationBuilder.add();

        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = otherDeviceConfiguration.createLoadProfileSpec(this.loadProfileType);
        loadProfileSpecBuilder.setOverruledObisCode(overruledChannelSpecObisCode);
        LoadProfileSpec otherLoadProfileSpec = loadProfileSpecBuilder.add();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(otherLoadProfileSpec);
        channelSpecBuilder.add();
    }

    @Test(expected = DuplicateChannelTypeException.class)
    @Transactional
    public void createWithSameChannelTypeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        createDefaultChannelSpec(loadProfileSpec);
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.add();
    }

    @Test(expected = DuplicateChannelTypeException.class)
    @Transactional
    public void createWithReadingTypeInUseAsPartOfCumulative() {
        // Can we detect that the current reading type is used by channel with a cumulative reading type?
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        createDefaultChannelSpec(loadProfileSpec);
        ChannelType channelTypeForCalculatedRT = loadProfileType.findChannelType(calculatedRegisterType).get();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelTypeForCalculatedRT, loadProfileSpec).overflow(BigDecimal.valueOf(999999L)).nbrOfFractionDigits(3);
        channelSpecBuilder.add();
    }

    @Test(expected = DuplicateChannelTypeException.class)
    @Transactional
    public void createWithReadingTypeInUse() {
        // Can we detect that the current cumulative reading type (or its calculated reading type) is already used by channel?
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelType channelTypeForCalculatedRT = loadProfileType.findChannelType(calculatedRegisterType).get();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelTypeForCalculatedRT, loadProfileSpec).overflow(BigDecimal.valueOf(999999L)).nbrOfFractionDigits(3);
        channelSpecBuilder.add();
        createDefaultChannelSpec(loadProfileSpec);
    }

    @Test(expected = DuplicateChannelTypeException.class)
    @Transactional
    public void createWithReadingTypeInUseByAnotherLoadProfileSpec() {
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME + "2", ObisCode.fromString("1.0.99.9.0.255"), interval, Arrays.asList(registerType));
        loadProfileType.save();
        deviceType.addLoadProfileType(loadProfileType);
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        createDefaultChannelSpec(loadProfileSpec);

        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = getReloadedDeviceConfiguration().createLoadProfileSpec(loadProfileType);
        loadProfileSpecBuilder.setOverruledObisCode(ObisCode.fromString("1.0.1.9.2.255"));
        loadProfileSpec = loadProfileSpecBuilder.add();
        createDefaultChannelSpec(loadProfileSpec);
    }

    @Test
    @Transactional
    public void successfulDeleteTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec channelSpec = createDefaultChannelSpec(loadProfileSpec);
        getReloadedDeviceConfiguration().removeChannelSpec(channelSpec);

        assertThat(getReloadedDeviceConfiguration().getChannelSpecs()).hasSize(0);
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    @Transactional
    public void deleteFromActiveDeviceConfigurationTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec channelSpec = createDefaultChannelSpec(loadProfileSpec);
        getReloadedDeviceConfiguration().activate();
        getReloadedDeviceConfiguration().removeChannelSpec(channelSpec);
    }


    @Test
    @Transactional
    public void cloneChannelSpecsWithoutOverruledObisCodeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec channelSpec = createDefaultChannelSpec(loadProfileSpec);
        DeviceConfiguration clone = deviceType.newConfiguration("MyClone").add();

        LoadProfileSpec lpSpecWithChannels = ((ServerLoadProfileSpec) loadProfileSpec).cloneForDeviceConfig(clone);
        assertThat(lpSpecWithChannels.getChannelSpecs()).hasSize(1);
        ChannelSpec clonedChannelSpec = lpSpecWithChannels.getChannelSpecs().get(0);
        assertThat(clonedChannelSpec.getObisCode()).isEqualTo(channelTypeObisCode);
        assertThat(clonedChannelSpec.getDeviceObisCode()).isEqualTo(channelTypeObisCode);
        assertThat(clonedChannelSpec.getDeviceConfiguration().getId()).isEqualTo(clone.getId());
        assertThat(clonedChannelSpec.getChannelType().getId()).isEqualTo(channelType.getId());
        assertThat(clonedChannelSpec.getInterval()).isEqualTo(channelType.getInterval());
        assertThat(clonedChannelSpec.getNbrOfFractionDigits()).isEqualTo(channelSpec.getNbrOfFractionDigits());
        assertThat(clonedChannelSpec.getOverflow().get().compareTo(channelSpec.getOverflow().get()) == 0).isTrue();
    }

    @Test
    @Transactional
    public void cloneChannelSpecsWithOverruledObisCodeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ObisCode deviceChannelObisCode = ObisCode.fromString("1.2.3.4.55.6");
        ChannelSpec channelSpec = createChannelSpecBuilder(loadProfileSpec).overruledObisCode(deviceChannelObisCode).add();
        DeviceConfiguration clone = deviceType.newConfiguration("MyClone").add();

        LoadProfileSpec lpSpecWithChannels = ((ServerLoadProfileSpec) loadProfileSpec).cloneForDeviceConfig(clone);
        assertThat(lpSpecWithChannels.getChannelSpecs()).hasSize(1);
        ChannelSpec clonedChannelSpec = lpSpecWithChannels.getChannelSpecs().get(0);
        assertThat(clonedChannelSpec.getObisCode()).isEqualTo(channelTypeObisCode);
        assertThat(clonedChannelSpec.getDeviceObisCode()).isEqualTo(deviceChannelObisCode);
        assertThat(clonedChannelSpec.getDeviceConfiguration().getId()).isEqualTo(clone.getId());
        assertThat(clonedChannelSpec.getChannelType().getId()).isEqualTo(channelType.getId());
        assertThat(clonedChannelSpec.getInterval()).isEqualTo(channelType.getInterval());
        assertThat(clonedChannelSpec.getNbrOfFractionDigits()).isEqualTo(channelSpec.getNbrOfFractionDigits());
        assertThat(clonedChannelSpec.getOverflow().get().compareTo(channelSpec.getOverflow().get()) == 0).isTrue();
    }

    @Test
    @Transactional
    public void cloneChannelSpecsWithMultiplierTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.useMultiplierWithCalculatedReadingType(readingTypeActiveDailyEnergyPrimaryMeteredDelta);
        channelSpecBuilder.add();
        DeviceConfiguration clone = deviceType.newConfiguration("MyClone").add();

        LoadProfileSpec lpSpecWithChannels = ((ServerLoadProfileSpec) loadProfileSpec).cloneForDeviceConfig(clone);
        assertThat(lpSpecWithChannels.getChannelSpecs()).hasSize(1);
        ChannelSpec clonedChannelSpec = lpSpecWithChannels.getChannelSpecs().get(0);
        assertThat(clonedChannelSpec.isUseMultiplier()).isTrue();
        assertThat(clonedChannelSpec.getCalculatedReadingType().get().getMRID()).isEqualTo(readingTypeActiveDailyEnergyPrimaryMeteredDelta.getMRID());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CALCULATED_READINGTYPE_CANNOT_BE_EMPTY +"}")
    public void calculatedReadingTypeIsRequiredWhenMultiplierIsTrueTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.useMultiplierWithCalculatedReadingType(null);
        channelSpecBuilder.add();
    }
    @Test
    @Transactional
    public void calculatedReadingTypeIsRequiredWhenMultiplierIsTrueWithoutViolationsTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.useMultiplierWithCalculatedReadingType(readingTypeActiveDailyEnergyPrimaryMeteredDelta);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    public void calculatedReadingTypeIsRequiredWhenMultiplierIsTrueWithoutViolationsForUpdateTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        ChannelSpec channelSpec = channelSpecBuilder.add();

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.useMultiplierWithCalculatedReadingType(readingTypeActiveDailyEnergyPrimaryMeteredDelta);
        channelSpecUpdater.update();
    }
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CALCULATED_READINGTYPE_CANNOT_BE_EMPTY +"}")
    public void calculatedReadingTypeIsRequiredWhenMultiplierIsTrueForUpdateTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        ChannelSpec channelSpec = channelSpecBuilder.add();

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.useMultiplierWithCalculatedReadingType(null);
        channelSpecUpdater.update();
    }

    private ChannelSpec.ChannelSpecBuilder createChannelSpecBuilder(LoadProfileSpec loadProfileSpec) {
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
        channelSpecBuilder.overflow(overflow);
        channelSpecBuilder.nbrOfFractionDigits(numberOfFractionDigits);
        return channelSpecBuilder;
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CALCULATED_READINGTYPE_DOES_NOT_MATCH_CRITERIA +"}")
    public void calculatedReadingTypeDoesNotMatchCriteriaTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.useMultiplierWithCalculatedReadingType(invalidReadingTypeActiveEnergyPrimaryMetered);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.READINGTYPE_CAN_NOT_BE_MULTIPLIED +"}")
    public void readingTypeCanNotBeMultipliedTest() {
        ChannelType channelType = loadProfileType.findChannelType(registerTypeWhichCanNotBeMultiplied).get();
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec).overflow(BigDecimal.valueOf(999999L)).nbrOfFractionDigits(3);
        channelSpecBuilder.useMultiplierWithCalculatedReadingType(readingTypeActiveDailyEnergyPrimaryMeteredDelta);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CANNOT_CHANGE_THE_USAGE_OF_THE_MULTIPLIER_OF_ACTIVE_CONFIG +"}")
    public void multiplierUsageCanNotBeUpdatedOnActiveConfigTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.useMultiplierWithCalculatedReadingType(readingTypeActiveDailyEnergyPrimaryMeteredDelta);
        ChannelSpec channelSpec = channelSpecBuilder.add();
        getReloadedDeviceConfiguration().activate();

        getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(inMemoryPersistence.getDeviceConfigurationService().findChannelSpec(channelSpec.getId()).get()).noMultiplier().update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CANNOT_CHANGE_MULTIPLIER_OF_ACTIVE_CONFIG +"}", strict = false)
    public void multiplierCanNotBeUpdatedOnActiveConfigTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = createChannelSpecBuilder(loadProfileSpec);
        channelSpecBuilder.useMultiplierWithCalculatedReadingType(readingTypeActiveDailyEnergyPrimaryMeteredDelta);
        ChannelSpec channelSpec = channelSpecBuilder.add();
        getReloadedDeviceConfiguration().activate();

        getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(inMemoryPersistence.getDeviceConfigurationService().findChannelSpec(channelSpec.getId()).get()).useMultiplierWithCalculatedReadingType(readingTypeActiveEnergySecondaryMetered).update();
    }
}
