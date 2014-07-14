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
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.OverFlowValueCanNotExceedNumberOfDigitsException;
import com.energyict.mdc.device.config.exceptions.OverFlowValueHasIncorrectFractionDigitsException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.google.common.base.Optional;
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
public class RegisterSpecImplTest extends DeviceTypeProvidingPersistenceTest {

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
        this.phenomenon1 = this.createPhenomenonIfMissing(this.unit1);
        this.phenomenon2 = this.createPhenomenonIfMissing(this.unit2);

        String code2 = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        String code1 = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code1).get();
        Optional<RegisterMapping> registerMappingByObisCodeAndUnitAndTimeOfUse =
                inMemoryPersistence.getMasterDataService()
                    .findRegisterMappingByObisCodeAndUnitAndTimeOfUse(registerMappingObisCode, unit1, readingType1.getTou());
        if (!registerMappingByObisCodeAndUnitAndTimeOfUse.isPresent()) {
            this.registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(REGISTER_MAPPING_NAME, registerMappingObisCode, unit1, readingType1, readingType1.getTou());
            this.registerMapping.save();
        }
        else {
            this.registerMapping = registerMappingByObisCodeAndUnitAndTimeOfUse.get();
        }

        // Business method
        this.deviceType.setDescription("For registerSpec Test purposes only");
        this.deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        this.deviceConfiguration = deviceConfigurationBuilder.add();
        this.deviceType.save();
    }

    private Phenomenon createPhenomenonIfMissing(Unit unit) {
        Optional<Phenomenon> xPhenomenon = inMemoryPersistence.getMasterDataService().findPhenomenonByUnit(unit);
        if (!xPhenomenon.isPresent()) {
            Phenomenon phenomenon = inMemoryPersistence.getMasterDataService().newPhenomenon(RegisterSpecImplTest.class.getSimpleName(), unit);
            phenomenon.save();
            return phenomenon;
        }
        else {
            return xPhenomenon.get();
        }
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


    private DeviceConfiguration getReloadedDeviceConfiguration(){
        return inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(this.deviceConfiguration.getId());
    }

    @Test
    @Transactional
    public void createRegisterSpecTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        assertThat(registerSpec.getRegisterMapping().getId()).isEqualTo(registerMapping.getId());
        assertThat(registerSpec.getObisCode()).isEqualTo(registerMappingObisCode);
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(registerMappingObisCode);
        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(this.getReloadedDeviceConfiguration().getId());
        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(this.numberOfDigits);
        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(this.numberOfFractionDigits);
        assertThat(registerSpec.getOverflowValue()).isEqualTo(BigDecimal.valueOf(1000000000));
        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
        assertThat(registerSpec.getMultiplierMode()).isEqualTo(MultiplierMode.NONE);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS+"}", property = "numberOfFractionDigits")
    public void createRegisterSpecNoFractionDigits() {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping).setNumberOfDigits(1).add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS+"}", property = "numberOfDigits")
    public void createRegisterSpecNegativeDigits() {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping).setNumberOfDigits(-1).setNumberOfFractionDigits(1).setOverflow(BigDecimal.ONE).add();
    }

    @Test
    @Transactional
    public void createRegisterSpecTestMultiplierDefaultToOne() {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping).setNumberOfDigits(1).setNumberOfFractionDigits(0).add();

        assertThat(registerSpec.getRegisterMapping().getId()).isEqualTo(registerMapping.getId());
        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
        assertThat(registerSpec.getMultiplierMode()).isEqualTo(MultiplierMode.CONFIGURED_ON_OBJECT);
    }

    @Test
    @Transactional
    public void createRegisterSpecTestOverflowDefaultIsApplied() {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping).setNumberOfDigits(5).setNumberOfFractionDigits(0).add();

        assertThat(registerSpec.getOverflowValue()).isEqualTo(BigDecimal.valueOf(100000));
    }

    @Test
    @Transactional
    public void createRegisterSpecTestOverflowDefaultIsAppliedLargeValue() {
        // JP-2164
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping).setNumberOfDigits(20).setNumberOfFractionDigits(0).add();

        assertThat(registerSpec.getOverflowValue()).isEqualTo(BigDecimal.TEN.pow(20));
    }

    @Test
    @Transactional
    public void updateNumberOfDigitsRegisterSpecTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        int updatedNumberOfDigits = 18;

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();

        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(updatedNumberOfDigits);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS+"}", property = "numberOfDigits")
    public void updateNumberOfDigitsRegisterSpecTooLargeTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        int updatedNumberOfDigits = 98;

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_OVERFLOW_IS_REQUIRED+"}", property = "overflow")
    public void updateOverflowMissing() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflow(null);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_OVERFLOW_VALUE+"}", property = "overflow")
    public void updateOverflowValueTooSmallTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflow(BigDecimal.ZERO);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_MULTIPLIER_VALUE+"}", property = "multiplier")
    public void updateMultiplierValueTooSmallTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setMultiplier(BigDecimal.ZERO);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS+"}", property = "numberOfFractionDigits", strict = false)
    public void setNegativeNumberOfFractionDigitsTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfFractionDigits(-1);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS+"}", property = "numberOfDigits", strict = false)
    public void setNegativeNumberOfDigitsTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        int updatedNumberOfDigits = -1;

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    public void updateNumberOfFractionDigitsRegisterSpecTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        int updatedNumberOfFractionDigits = 6;

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfFractionDigits(updatedNumberOfFractionDigits);
        registerSpecUpdater.update();

        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(updatedNumberOfFractionDigits);
    }

    @Test
    @Transactional
    public void updateWithOverruledObisCodeTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        ObisCode overruledObisCode = ObisCode.fromString("1.0.2.8.3.255");

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverruledObisCode(overruledObisCode);
        registerSpecUpdater.update();

        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(overruledObisCode);
        assertThat(registerSpec.getObisCode()).isEqualTo(this.registerMappingObisCode);
    }

    @Test
    @Transactional
    public void updateMultiplierModeTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setMultiplierMode(MultiplierMode.NONE);
        registerSpecUpdater.update();

        assertThat(registerSpec.getMultiplierMode()).isEqualTo(MultiplierMode.NONE);
    }

    @Test
    @Transactional
    public void updateMultiplierTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        BigDecimal multiplier = new BigDecimal("123.32");

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setMultiplier(multiplier);
        registerSpecUpdater.update();

        assertThat(registerSpec.getMultiplier()).isEqualTo(multiplier);
    }

    @Test
    @Transactional
    public void updateOverflowValueTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        BigDecimal overflow = new BigDecimal(456789);

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflow(overflow);
        registerSpecUpdater.update();

        assertThat(registerSpec.getOverflowValue()).isEqualTo(overflow);
    }

    @Test(expected = OverFlowValueCanNotExceedNumberOfDigitsException.class)
    @Transactional
    public void updateOverflowLargerThanNumberOfDigitsTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        BigDecimal overflow = new BigDecimal(1000000001);

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflow(overflow);
        registerSpecUpdater.update();
    }

    @Test(expected = OverFlowValueHasIncorrectFractionDigitsException.class)
    @Transactional
    public void updateWithIncorrectNumberOfFractionDigitsTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        BigDecimal overflow = new BigDecimal(123.33333333); // assuming we have three fractionDigits

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflow(overflow);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    public void deleteTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        this.getReloadedDeviceConfiguration().deleteRegisterSpec(registerSpec);

        assertThat(this.getReloadedDeviceConfiguration().getRegisterSpecs()).hasSize(0);
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    @Transactional
    public void deleteFromActiveConfigTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();

        this.getReloadedDeviceConfiguration().activate();
        this.getReloadedDeviceConfiguration().deleteRegisterSpec(registerSpec);
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
        RegisterMapping otherMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping("OtherMapping", ObisCode.fromString("1.2.3.1.5.6"), unit2, readingType2, readingType2.getTou());
        otherMapping.save();
        this.deviceType.addRegisterMapping(otherMapping);
        this.deviceType.save();
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.getReloadedDeviceConfiguration().createRegisterSpec(otherMapping);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec2 = registerSpecBuilder.add();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec2);
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
//        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.getReloadedDeviceConfiguration().createRegisterSpec(otherMapping);
//        setRegisterSpecDefaultFields(registerSpecBuilder);
//        registerSpec2 = registerSpecBuilder.add();
//    }

    @Test(expected = RegisterMappingIsNotConfiguredOnDeviceTypeException.class)
    @Transactional
    public void addSpecForMappingWhichIsNotOnDeviceTypeTest() {
        RegisterSpec registerSpec;
        RegisterMapping otherMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping("OtherMapping", ObisCode.fromString("32.12.32.5.12.32"), unit2, readingType2, readingType2.getTou());
        otherMapping.save();
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.getReloadedDeviceConfiguration().createRegisterSpec(otherMapping);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec = registerSpecBuilder.add();
    }

    @Test
    @Transactional
    public void validateMultiplierModeIsByDefaultSetToCONFIGUREDONOBJECTTest() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping);
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
        registerSpec = registerSpecBuilder.add();

        assertThat(registerSpec.getMultiplierMode()).isEqualTo(MultiplierMode.CONFIGURED_ON_OBJECT);
    }

    @Test
    @Transactional
    public void validateMultiplierIsByDefaultSetToONEByDefaultTest() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping);
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
        registerSpec = registerSpecBuilder.add();

        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void multiplierAutoSetToONEIfModeIsNoneTest() {
        RegisterSpec registerSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping);
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
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping);
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
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping);
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
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        registerSpecBuilder.setMultiplier(BigDecimal.TEN);
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
        registerSpec = registerSpecBuilder.add();

        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_DIGITS_DECREASED+"}", property = "numberOfDigits")
    public void testDecreaseNumberOfDigits() throws Exception {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping).setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).setMultiplier(BigDecimal.ONE).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        registerSpec.setNumberOfDigits(8); // decreased!!
        registerSpec.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED+"}", property = "numberOfFractionDigits")
    public void testDecreaseNumberOfFractionDigits() throws Exception {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping).setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).setMultiplier(BigDecimal.ONE).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        registerSpec.setNumberOfFractionDigits(1); // decreased!!
        registerSpec.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_MULTIPLIER_ACTIVE_DEVICE_CONFIG+"}", property = "multiplier")
    public void testUpdateMultiplierForActiveConfig() throws Exception {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping).setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).setMultiplier(BigDecimal.ONE).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        getReloadedDeviceConfiguration().save();
        getReloadedDeviceConfiguration().activate();
        registerSpec.setMultiplier(BigDecimal.valueOf(101)); // changed!
        registerSpec.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_REGISTER_MAPPING_ACTIVE_DEVICE_CONFIG+"}", property = "registerMapping")
    public void testUpdateRegisterMappingForActiveConfig() throws Exception {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createRegisterSpec(registerMapping).setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).setMultiplier(BigDecimal.ONE).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        getReloadedDeviceConfiguration().save();
        getReloadedDeviceConfiguration().activate();
        RegisterMapping registerMapping2 = inMemoryPersistence.getMasterDataService().newRegisterMapping(REGISTER_MAPPING_NAME + "2", registerMappingObisCode, unit2, readingType2, readingType2.getTou());
        registerMapping2.save();

        registerSpec.setRegisterMapping(registerMapping2); // updated
        registerSpec.save();
    }

    @Test
    @Transactional
    @Expected(value = OverFlowValueCanNotExceedNumberOfDigitsException.class)
    public void testVeryBigOverflowValueExceedsMaxInt() throws Exception {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().
                createRegisterSpec(registerMapping).
                setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).
                setMultiplier(BigDecimal.ONE).
                setNumberOfDigits(10).
                setNumberOfFractionDigits(3).
                setOverflow(BigDecimal.valueOf(Long.MAX_VALUE)).
                add();
        getReloadedDeviceConfiguration().save();
    }

    @Test
    @Transactional
    public void testVeryBigOverflowValueOverflowsToNegativeInt() throws Exception {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().
                createRegisterSpec(registerMapping).
                setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT).
                setMultiplier(BigDecimal.ONE).
                setNumberOfDigits(10).
                setNumberOfFractionDigits(3).
                setOverflow(BigDecimal.valueOf(Integer.MAX_VALUE).add(BigDecimal.valueOf(1000))).
                add();
        getReloadedDeviceConfiguration().save();
    }
}