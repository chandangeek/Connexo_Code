package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.InvalidValueException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.OverFlowValueCanNotExceedNumberOfDigitsException;
import com.energyict.mdc.device.config.exceptions.OverFlowValueHasIncorrectFractionDigitsException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.FlowDirection.REVERSE;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the {@link RegisterSpecImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 15:51
 */
public class RegisterSpecImplTest extends PersistenceTest {

    private static final String DEVICE_CONFIGURATION_NAME = RegisterSpecImplTest.class.getName() + "Config";
    private static final String REGISTER_MAPPING_NAME = RegisterSpecImplTest.class.getSimpleName() + "RegisterMapping";

    private final ObisCode registerMappingObisCode = ObisCode.fromString("1.0.1.8.0.255");
    private final ObisCode overruledRegisterSpecObisCode = ObisCode.fromString("1.0.1.8.2.255");
    private final int numberOfDigits = 9;
    private final int numberOfFractionDigits = 3;

    private DeviceConfiguration deviceConfiguration;
    private RegisterMapping registerMapping;
    private ReadingType readingType1;
    private ReadingType readingType2;
    private Unit unit1 = Unit.get("kWh");
    private Unit unit2 = Unit.get("MWh");
    private Phenomenon phenomenon1;
    private Phenomenon phenomenon2;

    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithRegisterSpecAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithRegisterSpecAndDeviceConfiguration() {
        this.phenomenon1 = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(RegisterSpecImplTest.class.getSimpleName(), unit1);
        this.phenomenon1.save();
        this.phenomenon2 = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(RegisterSpecImplTest.class.getSimpleName()+"2", unit2);
        this.phenomenon2.save();

        String code2 = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        String code1 = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code1).get();
        this.registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(REGISTER_MAPPING_NAME, registerMappingObisCode, unit1, readingType1, readingType1.getTou());
        this.registerMapping.save();

        // Business method
        this.deviceType.setDescription("For registerSpec Test purposes only");
        this.deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        this.deviceConfiguration = deviceConfigurationBuilder.add();
        this.deviceType.save();
    }

    private RegisterSpec createDefaultRegisterSpec() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec = registerSpecBuilder.add();
        return registerSpec;
    }

    private void setRegisterSpecDefaultFields(RegisterSpec.RegisterSpecBuilder registerSpecBuilder) {
        registerSpecBuilder.setMultiplierMode(MultiplierMode.NONE);
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
    }

    @Test
    @Transactional
    public void createRegisterSpecTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        assertThat(registerSpec.getRegisterMapping()).isEqualTo(registerMapping);
        assertThat(registerSpec.getObisCode()).isEqualTo(registerMappingObisCode);
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(registerMappingObisCode);
        assertThat(registerSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(this.numberOfDigits);
        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(this.numberOfFractionDigits);
        assertThat(registerSpec.getOverflowValue()).isEqualTo(BigDecimal.valueOf(1000000000));
        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
        assertThat(registerSpec.getMultiplierMode()).isEqualTo(MultiplierMode.NONE);
    }

    @Test
    @Transactional
    public void createRegisterSpecTestMultiplierDefaultToOne() {
        RegisterSpec registerSpec = this.deviceConfiguration.createRegisterSpec(registerMapping).setNumberOfDigits(1).add();

        assertThat(registerSpec.getRegisterMapping()).isEqualTo(registerMapping);
        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
        assertThat(registerSpec.getMultiplierMode()).isEqualTo(MultiplierMode.CONFIGURED_ON_OBJECT);
    }

    @Test
    @Transactional
    public void createRegisterSpecTestOverflowDefaultIsApplied() {
        RegisterSpec registerSpec = this.deviceConfiguration.createRegisterSpec(registerMapping).setNumberOfDigits(5).add();

        assertThat(registerSpec.getOverflowValue()).isEqualTo(BigDecimal.valueOf(100000));
    }

    @Test
    @Transactional
    public void updateNumberOfDigitsRegisterSpecTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        int updatedNumberOfDigits = 18;

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();

        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(updatedNumberOfDigits);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Constants.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS+"}", property = RegisterSpecImpl.NUMBER_OF_DIGITS)
    public void updateNumberOfDigitsRegisterSpecTooLargeTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        int updatedNumberOfDigits = 98;

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Constants.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS+"}", property = RegisterSpecImpl.NUMBER_OF_DIGITS, strict = false)
    public void setNegativeNumberOfDigitsTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        int updatedNumberOfDigits = -1;

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    public void updateNumberOfFractionDigitsRegisterSpecTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        int updatedNumberOfFractionDigits = 123;

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfFractionDigits(updatedNumberOfFractionDigits);
        registerSpecUpdater.update();

        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(updatedNumberOfFractionDigits);
    }

    @Test
    @Transactional
    public void updateWithOverruledObisCodeTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        ObisCode overruledObisCode = ObisCode.fromString("1.0.2.8.3.255");

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverruledObisCode(overruledObisCode);
        registerSpecUpdater.update();

        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(overruledObisCode);
        assertThat(registerSpec.getObisCode()).isEqualTo(this.registerMappingObisCode);
    }

    @Test
    @Transactional
    public void updateMultiplierModeTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setMultiplierMode(MultiplierMode.NONE);
        registerSpecUpdater.update();

        assertThat(registerSpec.getMultiplierMode()).isEqualTo(MultiplierMode.NONE);
    }

    @Test
    @Transactional
    public void updateMultiplierTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        BigDecimal multiplier = new BigDecimal("123.32");

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setMultiplier(multiplier);
        registerSpecUpdater.update();

        assertThat(registerSpec.getMultiplier()).isEqualTo(multiplier);
    }

    @Test
    @Transactional
    public void updateOverflowValueTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        BigDecimal overflow = new BigDecimal(456789);

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflow(overflow);
        registerSpecUpdater.update();

        assertThat(registerSpec.getOverflowValue()).isEqualTo(overflow);
    }

    @Test(expected = OverFlowValueCanNotExceedNumberOfDigitsException.class)
    @Transactional
    public void updateOverflowLargerThanNumberOfDigitsTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        BigDecimal overflow = new BigDecimal(1000000001);

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflow(overflow);
        registerSpecUpdater.update();
    }

    @Test(expected = InvalidValueException.class)
    @Transactional
    public void updateOverflowZeroTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        BigDecimal overflow = new BigDecimal(0);

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflow(overflow);
        registerSpecUpdater.update();
    }

    @Test(expected = OverFlowValueHasIncorrectFractionDigitsException.class)
    @Transactional
    public void updateWithIncorrectNumberOfFractionDigitsTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        BigDecimal overflow = new BigDecimal(123.33333333); // assuming we have three fractionDigits

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflow(overflow);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    public void deleteTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        this.deviceConfiguration.deleteRegisterSpec(registerSpec);

        assertThat(this.deviceConfiguration.getRegisterSpecs()).hasSize(0);
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    @Transactional
    public void deleteFromActiveConfigTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        this.deviceConfiguration.activate();
        this.deviceConfiguration.deleteRegisterSpec(registerSpec);
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithSameRegisterMappingTest() {
        RegisterSpec registerSpec1 = createDefaultRegisterSpec();
        RegisterSpec registerSpec2 = createDefaultRegisterSpec();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void updateWithSameObisCodeTest() {
        RegisterSpec registerSpec1 = createDefaultRegisterSpec();
        RegisterSpec registerSpec2;
        RegisterMapping otherMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("OtherMapping", ObisCode.fromString("1.2.3.1.5.6"), unit2, readingType2, readingType2.getTou());
        otherMapping.save();
        this.deviceType.addRegisterMapping(otherMapping);
        this.deviceType.save();
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(otherMapping);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec2 = registerSpecBuilder.add();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec2);
        registerSpecUpdater.setOverruledObisCode(registerMappingObisCode);
        registerSpecUpdater.update();
    }

//    @Test(expected = DuplicateObisCodeException.class)
//    @Transactional
//    public void addTwoSpecsWithDifferentMappingButSameObisCodeTest() {
//        RegisterSpec registerSpec1 = createDefaultRegisterSpec();
//        RegisterSpec registerSpec2;
//        RegisterMapping otherMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("OtherMapping", registerMappingObisCode, unit1, readingType1, readingType1.getTou());
//        otherMapping.save();
//        this.deviceType.addRegisterMapping(otherMapping);
//        this.deviceType.save();
//        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(otherMapping);
//        setRegisterSpecDefaultFields(registerSpecBuilder);
//        registerSpec2 = registerSpecBuilder.add();
//    }

    @Test(expected = RegisterMappingIsNotConfiguredOnDeviceTypeException.class)
    @Transactional
    public void addSpecForMappingWhichIsNotOnDeviceTypeTest() {
        RegisterSpec registerSpec;
        RegisterMapping otherMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("OtherMapping", ObisCode.fromString("32.12.32.5.12.32"), unit2, readingType2, readingType2.getTou());
        otherMapping.save();
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(otherMapping);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec = registerSpecBuilder.add();
    }

    @Test
    @Transactional
    public void validateMultiplierModeIsByDefaultSetToCONFIGUREDONOBJECTTest() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
        registerSpec = registerSpecBuilder.add();

        assertThat(registerSpec.getMultiplierMode()).isEqualTo(MultiplierMode.CONFIGURED_ON_OBJECT);
    }

    @Test
    @Transactional
    public void validateMultiplierIsByDefaultSetToONEByDefaultTest() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
        registerSpec = registerSpecBuilder.add();

        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void multiplierAutoSetToONEIfModeIsNoneTest() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.NONE);
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
        registerSpec = registerSpecBuilder.add();

        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void multiplierAutoSetBackToONEWhenModeIsSetToNONETest() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        registerSpecBuilder.setMultiplier(BigDecimal.TEN);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.NONE); // should erase the previously set multiplier
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
        registerSpec = registerSpecBuilder.add();

        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void multiplierAutoSetBackToONEWhenModeIsSetToVERSIONEDTest() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        registerSpecBuilder.setMultiplier(BigDecimal.TEN);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.VERSIONED); // should erase the previously set multiplier
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
        registerSpec = registerSpecBuilder.add();

        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void validateMultiplierIsChangeableWhenModeIsCONFIGUREDONOBJECTTest() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        registerSpecBuilder.setMultiplier(BigDecimal.TEN);
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
        registerSpec = registerSpecBuilder.add();

        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Constants.REGISTER_SPEC_NUMBER_OF_DIGITS_DECREASED+"}", property = RegisterSpecImpl.NUMBER_OF_DIGITS)
    public void testDecreaseNumberOfDigits() throws Exception {
        RegisterSpec registerSpec = this.deviceConfiguration.createRegisterSpec(registerMapping).setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).setMultiplier(BigDecimal.ONE).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        registerSpec.setNumberOfDigits(8); // decreased!!
        registerSpec.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Constants.REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED+"}", property = RegisterSpecImpl.NUMBER_OF_FRACTION_DIGITS)
    public void testDecreaseNumberOfFractionDigits() throws Exception {
        RegisterSpec registerSpec = this.deviceConfiguration.createRegisterSpec(registerMapping).setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).setMultiplier(BigDecimal.ONE).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        registerSpec.setNumberOfFractionDigits(1); // decreased!!
        registerSpec.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Constants.REGISTER_SPEC_MULTIPLIER_ACTIVE_DEVICE_CONFIG+"}", property = RegisterSpecImpl.MULTIPLIER)
    public void testUpdateMultiplierForActiveConfig() throws Exception {
        RegisterSpec registerSpec = this.deviceConfiguration.createRegisterSpec(registerMapping).setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).setMultiplier(BigDecimal.ONE).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        deviceConfiguration.activate();
        deviceConfiguration.save();
        registerSpec.setMultiplier(BigDecimal.valueOf(101)); // changed!
        registerSpec.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Constants.REGISTER_SPEC_REGISTER_MAPPING_ACTIVE_DEVICE_CONFIG+"}", property = RegisterSpecImpl.REGISTER_MAPPING)
    public void testUpdateRegisterMappingForActiveConfig() throws Exception {
        RegisterSpec registerSpec = this.deviceConfiguration.createRegisterSpec(registerMapping).setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).setMultiplier(BigDecimal.ONE).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        deviceConfiguration.activate();
        deviceConfiguration.save();
        RegisterMapping registerMapping2 = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(REGISTER_MAPPING_NAME + "2", registerMappingObisCode, unit2, readingType2, readingType2.getTou());
        registerMapping2.save();

        registerSpec.setRegisterMapping(registerMapping2); // updated
        registerSpec.save();
    }

    @Test
    @Transactional
    @Expected(value = OverFlowValueCanNotExceedNumberOfDigitsException.class)
    public void testVeryBigOverflowValueExceedsMaxInt() throws Exception {
        RegisterSpec registerSpec = this.deviceConfiguration.
                createRegisterSpec(registerMapping).
                setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).
                setMultiplier(BigDecimal.ONE).
                setNumberOfDigits(10).
                setNumberOfFractionDigits(3).
                setOverflow(BigDecimal.valueOf(Long.MAX_VALUE)).
                add();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    public void testVeryBigOverflowValueOverflowsToNegativeInt() throws Exception {
        RegisterSpec registerSpec = this.deviceConfiguration.
                createRegisterSpec(registerMapping).
                setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).
                setMultiplier(BigDecimal.ONE).
                setNumberOfDigits(10).
                setNumberOfFractionDigits(3).
                setOverflow(BigDecimal.valueOf(Integer.MAX_VALUE).add(BigDecimal.valueOf(1000))).
                add();
        deviceConfiguration.save();
    }
}