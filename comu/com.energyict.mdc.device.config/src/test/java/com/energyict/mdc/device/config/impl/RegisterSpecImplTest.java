/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.RegisterTypeIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.masterdata.RegisterType;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_PRIMARY_METERED;
import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.FlowDirection.REVERSE;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.fest.assertions.Assertions.assertThat;

public class RegisterSpecImplTest extends DeviceTypeProvidingPersistenceTest {

    private static final String DEVICE_CONFIGURATION_NAME = RegisterSpecImplTest.class.getName() + "Config";
    private static final String REGISTER_TYPE_NAME = RegisterSpecImplTest.class.getSimpleName() + "RegisterType";

    private final ObisCode registerTypeObisCode = ObisCode.fromString("1.0.1.8.0.255");
    private final ObisCode overruledRegisterSpecObisCode = ObisCode.fromString("1.0.1.8.2.255");
    private final int numberOfFractionDigits = 3;
    private final BigDecimal overflowValue = BigDecimal.valueOf(10000);

    private DeviceConfiguration deviceConfiguration;
    private RegisterType registerType;
    private RegisterType deltaRegisterType;
    private ReadingType readingType1;
    private ReadingType readingType2;
    private ReadingType readingType3;
    private ReadingType deltaReadingType;
    private final String invalidActiveEnergyPrimary = ReadingTypeCodeBuilder.of(ELECTRICITY_PRIMARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.BULKQUANTITY).code();
    private final ReadingType invalidReadingTypeActiveEnergyPrimaryMetered = inMemoryPersistence.getMeteringService().getReadingType(invalidActiveEnergyPrimary).get();

    private Unit unit1 = Unit.get("kWh");
    private Unit unit2 = Unit.get("MWh");

    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithRegisterSpecAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithRegisterSpecAndDeviceConfiguration() {
        String code2 = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.BULKQUANTITY).code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        String code1 = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.BULKQUANTITY).code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code1).get();
        String code3 = ReadingTypeCodeBuilder.of(ELECTRICITY_PRIMARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.BULKQUANTITY).code();
        this.readingType3 = inMemoryPersistence.getMeteringService().getReadingType(code3).get();
        this.registerType = createOrSetRegisterType(readingType1, registerTypeObisCode);

        String deltaCode = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.DELTADELTA).code();
        this.deltaReadingType = inMemoryPersistence.getMeteringService().getReadingType(deltaCode).get();
        this.deltaRegisterType = createOrSetRegisterType(deltaReadingType, registerTypeObisCode);

        // Business method
        this.deviceType.setDescription("For registerSpec Test purposes only");
        this.deviceType.addRegisterType(registerType);
        this.deviceType.addRegisterType(deltaRegisterType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        this.deviceConfiguration = deviceConfigurationBuilder.add();
    }

    private NumericalRegisterSpec createNumericalRegisterSpecWithDefaults() {
        NumericalRegisterSpec registerSpec;
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(registerType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec = registerSpecBuilder.add();
        return registerSpec;
    }

    private void setRegisterSpecDefaultFields(NumericalRegisterSpec.Builder registerSpecBuilder) {
        registerSpecBuilder.overflowValue(overflowValue);
        registerSpecBuilder.numberOfFractionDigits(numberOfFractionDigits);
    }

    private TextualRegisterSpec createTextualRegisterSpecWithDefaults() {
        TextualRegisterSpec registerSpec;
        TextualRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createTextualRegisterSpec(registerType);
        registerSpec = registerSpecBuilder.add();
        return registerSpec;
    }


    private DeviceConfiguration getReloadedDeviceConfiguration(){
        return inMemoryPersistence.getDeviceConfigurationService()
                .findDeviceConfiguration(this.deviceConfiguration.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload device configuration " + this.deviceConfiguration.getId()));
    }

    @Test
    @Transactional
    public void createNumericalRegisterSpecTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();

        assertThat(registerSpec.isTextual()).isFalse();
        assertThat(registerSpec.getRegisterType().getId()).isEqualTo(registerType.getId());
        assertThat(registerSpec.getObisCode()).isEqualTo(registerTypeObisCode);
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(registerTypeObisCode);
        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(this.getReloadedDeviceConfiguration().getId());
        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(this.numberOfFractionDigits);
        assertThat(registerSpec.getOverflowValue().get().compareTo(overflowValue) == 0).isTrue();
    }

    @Test
    @Transactional
    public void createTextualRegisterSpecTest() {
        TextualRegisterSpec registerSpec = createTextualRegisterSpecWithDefaults();

        assertThat(registerSpec.isTextual()).isTrue();
        assertThat(registerSpec.getRegisterType().getId()).isEqualTo(registerType.getId());
        assertThat(registerSpec.getObisCode()).isEqualTo(registerTypeObisCode);
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(registerTypeObisCode);
        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(this.getReloadedDeviceConfiguration().getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS+"}", property = "numberOfFractionDigits")
    public void createRegisterSpecNoFractionDigits() {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).overflowValue(overflowValue).add();
    }

    @Test
    @Transactional
    public void createRegisterSpecTestOverflowDefaultIsApplied() {
        NumericalRegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).overflowValue(overflowValue).numberOfFractionDigits(0).add();

        assertThat(registerSpec.getOverflowValue().get().compareTo(overflowValue) == 0).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.FIELD_IS_REQUIRED+"}", property = "overflow")
    public void updateOverflowMissing() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.overflowValue(null);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    public void noOverFlowForDeltaRegisterSpecs() {
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(deltaRegisterType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        NumericalRegisterSpec registerSpec = registerSpecBuilder.add();

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.overflowValue(null);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_OVERFLOW_VALUE+"}", property = "overflow")
    public void updateOverflowValueTooSmallTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();

        NumericalRegisterSpec.Updater registerSpecUpdater = getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.overflowValue(BigDecimal.ZERO);
        registerSpecUpdater.update();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_OVERFLOW_VALUE+"}", property = "overflow")
    public void overflowValueIsToSmallForDeltaRegisterType() {
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(deltaRegisterType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        NumericalRegisterSpec registerSpec = registerSpecBuilder.add();

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.overflowValue(BigDecimal.ZERO);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS+"}", property = "numberOfFractionDigits", strict = false)
    public void setNegativeNumberOfFractionDigitsTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();

        NumericalRegisterSpec.Updater registerSpecUpdater = getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.numberOfFractionDigits(-1);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    public void updateNumberOfFractionDigitsRegisterSpecTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        int updatedNumberOfFractionDigits = 6;

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.numberOfFractionDigits(updatedNumberOfFractionDigits);
        registerSpecUpdater.update();

        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(updatedNumberOfFractionDigits);
    }

    @Test
    @Transactional
    public void updateWithOverruledObisCodeTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        ObisCode overruledObisCode = ObisCode.fromString("1.0.2.8.3.255");

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.overruledObisCode(overruledObisCode);
        registerSpecUpdater.update();

        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(overruledObisCode);
        assertThat(registerSpec.getObisCode()).isEqualTo(this.registerTypeObisCode);
    }

    @Test
    @Transactional
    public void updateOverflowValueTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        BigDecimal overflow = new BigDecimal(456789);

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.overflowValue(overflow);
        registerSpecUpdater.update();

        assertThat(registerSpec.getOverflowValue().get().compareTo(overflow) == 0).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "DTC6007S The provided overflow value \"123.333\" more fraction digits \"46\" than provided \"3\"", property = "overflow")
    public void updateWithIncorrectNumberOfFractionDigitsTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        BigDecimal overflow = new BigDecimal(123.33333333); // assuming we have three fractionDigits

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.overflowValue(overflow);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    public void deleteNumericalTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();

        this.getReloadedDeviceConfiguration().deleteRegisterSpec(registerSpec);

        assertThat(this.getReloadedDeviceConfiguration().getRegisterSpecs()).hasSize(0);
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    @Transactional
    public void deleteFromActiveConfigTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();

        this.getReloadedDeviceConfiguration().activate();
        this.getReloadedDeviceConfiguration().deleteRegisterSpec(registerSpec);
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoNumericalSpecsWithSameRegisterTypeTest() {
        RegisterSpec registerSpec1 = createNumericalRegisterSpecWithDefaults();
        RegisterSpec registerSpec2 = createNumericalRegisterSpecWithDefaults();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoTextualSpecsWithSameRegisterTypeTest() {
        RegisterSpec registerSpec1 = createNumericalRegisterSpecWithDefaults();
        RegisterSpec registerSpec2 = createNumericalRegisterSpecWithDefaults();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithSameRegisterTypeTest() {
        RegisterSpec registerSpec1 = createNumericalRegisterSpecWithDefaults();
        RegisterSpec registerSpec2 = createTextualRegisterSpecWithDefaults();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void updateWithSameObisCodeTest() {
        NumericalRegisterSpec registerSpec1 = createNumericalRegisterSpecWithDefaults();
        NumericalRegisterSpec registerSpec2;
        RegisterType otherType = getRegisterType(readingType2);
        this.deviceType.addRegisterType(otherType);
        NumericalRegisterSpec.Builder registerSpecBuilder = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(otherType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec2 = registerSpecBuilder.add();

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec2);
        registerSpecUpdater.overruledObisCode(registerTypeObisCode);
        registerSpecUpdater.update();
    }

    private RegisterType getRegisterType(ReadingType readingType) {
        Optional<RegisterType> existingRegisterType = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType);
        RegisterType otherType;
        ObisCode otherObisCode = ObisCode.fromString("1.2.3.1.5.6");
        if(existingRegisterType.isPresent()){
            otherType = existingRegisterType.get();
            otherType.setObisCode(otherObisCode);
            otherType.save();
        } else {
            otherType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, otherObisCode);
        }
        otherType.save();
        return otherType;
    }

    @Test(expected = RegisterTypeIsNotConfiguredOnDeviceTypeException.class)
    @Transactional
    public void addSpecForMappingWhichIsNotOnDeviceTypeTest() {
        RegisterSpec registerSpec;
        RegisterType otherType = getRegisterType(readingType2);
        NumericalRegisterSpec.Builder registerSpecBuilder = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(otherType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec = registerSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED+"}", property = "numberOfFractionDigits")
    public void testDecreaseNumberOfFractionDigits() throws Exception {
        NumericalRegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType)
                .overflowValue(overflowValue).numberOfFractionDigits(3).add();
        this.getReloadedDeviceConfiguration().activate();
        registerSpec.getDeviceConfiguration().getRegisterSpecUpdaterFor(((NumericalRegisterSpec) inMemoryPersistence.getDeviceConfigurationService().findRegisterSpec(registerSpec.getId()).get())).numberOfFractionDigits(1).update();
    }

    @Test
    @Transactional
    public void testDecreaseNumberOfFractionDigitsInactiveConfig() throws Exception {
        NumericalRegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).overflowValue(overflowValue).numberOfFractionDigits(3).add();
        registerSpec.getDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec).numberOfFractionDigits(1).update();
    }

    @Test
    @Transactional
    public void cloneTextualWithOverruledObisCodeTest() {
        ObisCode deviceObisCode = ObisCode.fromString("1.9.1.8.17.255");
        TextualRegisterSpec.Builder builder = getReloadedDeviceConfiguration().createTextualRegisterSpec(registerType);
        builder.setOverruledObisCode(deviceObisCode);
        TextualRegisterSpec textualRegisterSpec = builder.add();

        DeviceConfiguration cloneConfig = deviceType.newConfiguration("MyClone").add();
        RegisterSpec registerSpec = ((ServerRegisterSpec) textualRegisterSpec).cloneForDeviceConfig(cloneConfig);

        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(cloneConfig.getId());
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(deviceObisCode);
        assertThat(registerSpec.getObisCode()).isEqualTo(registerType.getObisCode());
    }

    @Test
    @Transactional
    public void cloneTexturalRegisterWithoutOverruledObisCodeTest() {
        TextualRegisterSpec.Builder builder = getReloadedDeviceConfiguration().createTextualRegisterSpec(registerType);
        TextualRegisterSpec textualRegisterSpec = builder.add();

        DeviceConfiguration cloneConfig = deviceType.newConfiguration("MyClone").add();
        RegisterSpec registerSpec = ((ServerRegisterSpec) textualRegisterSpec).cloneForDeviceConfig(cloneConfig);

        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(cloneConfig.getId());
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(registerType.getObisCode());
        assertThat(registerSpec.getObisCode()).isEqualTo(registerType.getObisCode());
    }

    @Test
    @Transactional
    public void cloneNumericalRegisterWithOverruledObisCodeTest() {
        ObisCode deviceObisCode = ObisCode.fromString("1.9.1.8.17.255");
        BigDecimal myOverflowValue = BigDecimal.valueOf(65111L);
        int numberOfFractionDigits = 6;
        NumericalRegisterSpec.Builder builder = getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType);
        builder.overruledObisCode(deviceObisCode);
        builder.overflowValue(myOverflowValue);
        builder.numberOfFractionDigits(numberOfFractionDigits);
        NumericalRegisterSpec numericalRegisterSpec = builder.add();

        DeviceConfiguration cloneConfig = deviceType.newConfiguration("MyClone").add();
        NumericalRegisterSpec registerSpec = (NumericalRegisterSpec) ((NumericalRegisterSpecImpl) numericalRegisterSpec).cloneForDeviceConfig(cloneConfig);
        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(cloneConfig.getId());
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(deviceObisCode);
        assertThat(registerSpec.getObisCode()).isEqualTo(registerType.getObisCode());
        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(numberOfFractionDigits);
        assertThat(registerSpec.getOverflowValue().get().compareTo(myOverflowValue) == 0).isTrue();
    }

    @Test
    @Transactional
    public void cloneNumericalRegisterWithoutOverruledObisCodeTest() {
        BigDecimal overFlowValue = BigDecimal.valueOf(65111L);
        int numberOfFractionDigits = 6;
        NumericalRegisterSpec.Builder builder = getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType);
        builder.overflowValue(overFlowValue);
        builder.numberOfFractionDigits(numberOfFractionDigits);
        NumericalRegisterSpec numericalRegisterSpec = builder.add();

        DeviceConfiguration cloneConfig = deviceType.newConfiguration("MyClone").add();
        NumericalRegisterSpec registerSpec = (NumericalRegisterSpec) ((NumericalRegisterSpecImpl) numericalRegisterSpec).cloneForDeviceConfig(cloneConfig);
        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(cloneConfig.getId());
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(registerType.getObisCode());
        assertThat(registerSpec.getObisCode()).isEqualTo(registerType.getObisCode());
        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(numberOfFractionDigits);
        assertThat(registerSpec.getOverflowValue().get().compareTo(overFlowValue) == 0).isTrue();
    }


    @Test
    @Transactional
    public void cloneNumericalRegisterWithMultiplierTest() {
        ObisCode deviceObisCode = ObisCode.fromString("1.9.1.8.17.255");
        BigDecimal overFlowValue = BigDecimal.valueOf(65111L);
        int numberOfFractionDigits = 6;
        NumericalRegisterSpec.Builder builder = getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType);
        builder.overruledObisCode(deviceObisCode);
        builder.overflowValue(overFlowValue);
        builder.numberOfFractionDigits(numberOfFractionDigits);
        builder.useMultiplierWithCalculatedReadingType(readingType3);
        NumericalRegisterSpec numericalRegisterSpec = builder.add();

        DeviceConfiguration cloneConfig = deviceType.newConfiguration("MyClone").add();
        NumericalRegisterSpec registerSpec = (NumericalRegisterSpec) ((NumericalRegisterSpecImpl) numericalRegisterSpec).cloneForDeviceConfig(cloneConfig);
        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(cloneConfig.getId());
        assertThat(registerSpec.isUseMultiplier()).isTrue();
        assertThat(registerSpec.getCalculatedReadingType().get().getMRID()).isEqualTo(readingType3.getMRID()
        );
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CALCULATED_READINGTYPE_CANNOT_BE_EMPTY +"}")
    public void calculatedReadingTypeIsRequiredWhenMultiplierIsTrueTest() {
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(registerType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpecBuilder.useMultiplierWithCalculatedReadingType(null);
        registerSpecBuilder.add();
    }
    @Test
    @Transactional
    public void calculatedReadingTypeIsRequiredWhenMultiplierIsTrueWithoutViolationsTest() {
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(registerType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpecBuilder.useMultiplierWithCalculatedReadingType(readingType3);
        registerSpecBuilder.add();
    }

    @Test
    @Transactional
    public void calculatedReadingTypeIsRequiredWhenMultiplierIsTrueWithoutViolationsForUpdateTest() {
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(registerType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        NumericalRegisterSpec numericalRegisterSpec = registerSpecBuilder.add();

        NumericalRegisterSpec.Updater registerSpecUpdater = numericalRegisterSpec.getDeviceConfiguration().getRegisterSpecUpdaterFor(numericalRegisterSpec);
        registerSpecUpdater.useMultiplierWithCalculatedReadingType(readingType3);
        registerSpecUpdater.update();
    }
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CALCULATED_READINGTYPE_CANNOT_BE_EMPTY +"}")
    public void calculatedReadingTypeIsRequiredWhenMultiplierIsTrueForUpdateTest() {
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(registerType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        NumericalRegisterSpec numericalRegisterSpec = registerSpecBuilder.add();

        NumericalRegisterSpec.Updater registerSpecUpdater = numericalRegisterSpec.getDeviceConfiguration().getRegisterSpecUpdaterFor(numericalRegisterSpec);
        registerSpecUpdater.useMultiplierWithCalculatedReadingType(null);
        registerSpecUpdater.update();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CALCULATED_READINGTYPE_DOES_NOT_MATCH_CRITERIA +"}")
    public void calculatedReadingTypeDoesNotMatchCriteriaTest() {
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(deltaRegisterType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpecBuilder.useMultiplierWithCalculatedReadingType(invalidReadingTypeActiveEnergyPrimaryMetered);
        registerSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.READINGTYPE_CAN_NOT_BE_MULTIPLIED +"}")
    public void readingTypeCanNotBeMultipliedTest() {
        RegisterType registerTypeWhichCanNotBeMultiplied = createOrSetRegisterType(readingType3, registerTypeObisCode);

        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(registerTypeWhichCanNotBeMultiplied);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpecBuilder.useMultiplierWithCalculatedReadingType(readingType1);
        registerSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CANNOT_CHANGE_THE_USAGE_OF_THE_MULTIPLIER_OF_ACTIVE_CONFIG +"}")
    public void multiplierUsageCanNotBeUpdatedOnActiveConfigTest() {
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(registerType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpecBuilder.useMultiplierWithCalculatedReadingType(readingType3);
        NumericalRegisterSpec numericalRegisterSpec = registerSpecBuilder.add();

        getReloadedDeviceConfiguration().activate();
        getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(((NumericalRegisterSpec) inMemoryPersistence.getDeviceConfigurationService().findRegisterSpec(numericalRegisterSpec.getId()).get())).noMultiplier().update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CANNOT_CHANGE_MULTIPLIER_OF_ACTIVE_CONFIG +"}", strict = false)
    public void multiplierCanNotBeUpdatedOnActiveConfigTest() {
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(registerType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpecBuilder.useMultiplierWithCalculatedReadingType(readingType3);
        NumericalRegisterSpec numericalRegisterSpec = registerSpecBuilder.add();

        getReloadedDeviceConfiguration().activate();
        getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(((NumericalRegisterSpec) inMemoryPersistence.getDeviceConfigurationService().findRegisterSpec(numericalRegisterSpec.getId()).get())).useMultiplierWithCalculatedReadingType(readingType1).update();
    }
}