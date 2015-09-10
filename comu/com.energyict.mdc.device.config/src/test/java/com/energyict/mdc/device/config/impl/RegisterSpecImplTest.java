package com.energyict.mdc.device.config.impl;

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

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.*;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.FlowDirection.REVERSE;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the {@link RegisterSpecImpl} class hierarchy.
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 15:51
 */
public class RegisterSpecImplTest extends DeviceTypeProvidingPersistenceTest {

    private static final String DEVICE_CONFIGURATION_NAME = RegisterSpecImplTest.class.getName() + "Config";
    private static final String REGISTER_TYPE_NAME = RegisterSpecImplTest.class.getSimpleName() + "RegisterType";

    private final ObisCode registerTypeObisCode = ObisCode.fromString("1.0.1.6.0.255");
    private final ObisCode overruledRegisterSpecObisCode = ObisCode.fromString("1.0.1.8.2.255");
    private final int numberOfDigits = 9;
    private final int numberOfFractionDigits = 3;

    private DeviceConfiguration deviceConfiguration;
    private RegisterType registerType;
    private ReadingType readingType1;
    private ReadingType readingType2;
    private Unit unit1 = Unit.get("kWh");
    private Unit unit2 = Unit.get("MWh");

    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithRegisterSpecAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithRegisterSpecAndDeviceConfiguration() {

        String code2 = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.DELTADELTA).code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        String code1 = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.DELTADELTA).code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code1).get();
        Optional<RegisterType> registerTypeByObisCodeAndUnitAndTimeOfUse =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (!registerTypeByObisCodeAndUnitAndTimeOfUse.isPresent()) {
            this.registerType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType1, registerTypeObisCode);
            this.registerType.save();
        }
        else {
            this.registerType = registerTypeByObisCodeAndUnitAndTimeOfUse.get();
        }

        // Business method
        this.deviceType.setDescription("For registerSpec Test purposes only");
        this.deviceType.addRegisterType(registerType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        this.deviceConfiguration = deviceConfigurationBuilder.add();
        this.deviceType.save();
    }

    private NumericalRegisterSpec createNumericalRegisterSpecWithDefaults() {
        NumericalRegisterSpec registerSpec;
        NumericalRegisterSpec.Builder registerSpecBuilder = this.deviceConfiguration.createNumericalRegisterSpec(registerType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec = registerSpecBuilder.add();
        return registerSpec;
    }

    private void setRegisterSpecDefaultFields(NumericalRegisterSpec.Builder registerSpecBuilder) {
        registerSpecBuilder.setNumberOfDigits(numberOfDigits);
        registerSpecBuilder.setNumberOfFractionDigits(numberOfFractionDigits);
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
        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(this.numberOfDigits);
        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(this.numberOfFractionDigits);
        assertThat(registerSpec.getOverflowValue()).isEqualTo(BigDecimal.valueOf(1000000000));
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
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).setNumberOfDigits(1).add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS+"}", property = "numberOfDigits")
    public void createRegisterSpecNegativeDigits() {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).setNumberOfDigits(-1).setNumberOfFractionDigits(1).setOverflowValue(BigDecimal.ONE).add();
    }

    @Test
    @Transactional
    public void createRegisterSpecTestOverflowDefaultIsApplied() {
        NumericalRegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).setNumberOfDigits(5).setNumberOfFractionDigits(0).add();

        assertThat(registerSpec.getOverflowValue()).isEqualTo(BigDecimal.valueOf(100000));
    }

    @Test
    @Transactional
    public void createRegisterSpecTestOverflowDefaultIsAppliedLargeValue() {
        // JP-2164
        NumericalRegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).setNumberOfDigits(20).setNumberOfFractionDigits(0).add();

        assertThat(registerSpec.getOverflowValue()).isEqualTo(BigDecimal.TEN.pow(20));
    }

    @Test
    @Transactional
    public void updateNumberOfDigitsRegisterSpecTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        int updatedNumberOfDigits = 18;

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();

        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(updatedNumberOfDigits);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS+"}", property = "numberOfDigits")
    public void updateNumberOfDigitsRegisterSpecTooLargeTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        int updatedNumberOfDigits = 98;

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_OVERFLOW_IS_REQUIRED+"}", property = "overflow")
    public void updateOverflowMissing() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflowValue(null);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_OVERFLOW_VALUE+"}", property = "overflow")
    public void updateOverflowValueTooSmallTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();

        NumericalRegisterSpec.Updater registerSpecUpdater = getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflowValue(BigDecimal.ZERO);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS+"}", property = "numberOfFractionDigits", strict = false)
    public void setNegativeNumberOfFractionDigitsTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();

        NumericalRegisterSpec.Updater registerSpecUpdater = getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfFractionDigits(-1);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS+"}", property = "numberOfDigits", strict = false)
    public void setNegativeNumberOfDigitsTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        int updatedNumberOfDigits = -1;

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    public void updateNumberOfFractionDigitsRegisterSpecTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        int updatedNumberOfFractionDigits = 6;

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfFractionDigits(updatedNumberOfFractionDigits);
        registerSpecUpdater.update();

        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(updatedNumberOfFractionDigits);
    }

    @Test
    @Transactional
    public void updateWithOverruledObisCodeTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        ObisCode overruledObisCode = ObisCode.fromString("1.0.2.8.3.255");

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverruledObisCode(overruledObisCode);
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
        registerSpecUpdater.setOverflowValue(overflow);
        registerSpecUpdater.update();

        assertThat(registerSpec.getOverflowValue()).isEqualTo(overflow);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "DTC6005S The provided overflow value \"1,000,000,001\" may not exceed \"1,000,000,000\" (according to the provided number of digits \"9\")", property = "overflow")
    public void updateOverflowLargerThanNumberOfDigitsTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        BigDecimal overflow = new BigDecimal(1000000001);

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflowValue(overflow);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "DTC6007S The provided overflow value \"123.333\" more fraction digits \"46\" than provided \"3\"", property = "overflow")
    public void updateWithIncorrectNumberOfFractionDigitsTest() {
        NumericalRegisterSpec registerSpec = createNumericalRegisterSpecWithDefaults();
        BigDecimal overflow = new BigDecimal(123.33333333); // assuming we have three fractionDigits

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setOverflowValue(overflow);
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
        this.deviceType.save();
        NumericalRegisterSpec.Builder registerSpecBuilder = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(otherType);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec2 = registerSpecBuilder.add();

        NumericalRegisterSpec.Updater registerSpecUpdater = this.getReloadedDeviceConfiguration().getRegisterSpecUpdaterFor(registerSpec2);
        registerSpecUpdater.setOverruledObisCode(registerTypeObisCode);
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
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_DIGITS_DECREASED+"}", property = "numberOfDigits", strict = false)
    public void testDecreaseNumberOfDigits() throws Exception {
        NumericalRegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        this.getReloadedDeviceConfiguration().activate();
        registerSpec.setNumberOfDigits(8); // decreased!!
        registerSpec.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED+"}", property = "numberOfFractionDigits")
    public void testDecreaseNumberOfFractionDigits() throws Exception {
        NumericalRegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        this.getReloadedDeviceConfiguration().activate();
        registerSpec.setNumberOfFractionDigits(1); // decreased!!
        registerSpec.save();
    }

    @Test
    @Transactional
    public void testDecreaseNumberOfDigitsInactiveConfig() throws Exception {
        NumericalRegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        registerSpec.setNumberOfDigits(8); // decreased!!
        registerSpec.setOverflowValue(BigDecimal.valueOf(100000000));
        registerSpec.save();
    }

    @Test
    @Transactional
    public void testDecreaseNumberOfFractionDigitsInactiveConfig() throws Exception {
        NumericalRegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        registerSpec.setNumberOfFractionDigits(1); // decreased!!
        registerSpec.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.REGISTER_SPEC_REGISTER_TYPE_ACTIVE_DEVICE_CONFIG +"}", property = "registerType")
    public void testUpdateRegisterTypeForActiveConfig() throws Exception {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType).setNumberOfDigits(10).setNumberOfFractionDigits(3).add();
        getReloadedDeviceConfiguration().save();
        getReloadedDeviceConfiguration().activate();
        RegisterType registerType2 = getRegisterType(readingType2);

        registerSpec.setRegisterType(registerType2); // updated
        registerSpec.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "DTC6005S The provided overflow value \"9,223,372,036,854,775,807\" may not exceed \"10,000,000,000\" (according to the provided number of digits \"10\")", property = "overflow")
    public void testVeryBigOverflowValueExceedsMaxInt() throws Exception {
        this.getReloadedDeviceConfiguration().
                createNumericalRegisterSpec(registerType).
                setNumberOfDigits(10).
                setNumberOfFractionDigits(3).
                setOverflowValue(BigDecimal.valueOf(Long.MAX_VALUE)).
                add();
        getReloadedDeviceConfiguration().save();
    }

    @Test
    @Transactional
    public void testVeryBigOverflowValueOverflowsToNegativeInt() throws Exception {
        RegisterSpec registerSpec = this.getReloadedDeviceConfiguration().
                createNumericalRegisterSpec(registerType).
                setNumberOfDigits(10).
                setNumberOfFractionDigits(3).
                setOverflowValue(BigDecimal.valueOf(Integer.MAX_VALUE).add(BigDecimal.valueOf(1000))).
                add();
        getReloadedDeviceConfiguration().save();
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
        BigDecimal overFlowValue = BigDecimal.valueOf(65111L);
        int numberOfDigits = 7;
        int numberOfFractionDigits = 6;
        NumericalRegisterSpec.Builder builder = getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType);
        builder.setOverruledObisCode(deviceObisCode);
        builder.setOverflowValue(overFlowValue);
        builder.setNumberOfDigits(numberOfDigits);
        builder.setNumberOfFractionDigits(numberOfFractionDigits);
        NumericalRegisterSpec numericalRegisterSpec = builder.add();

        DeviceConfiguration cloneConfig = deviceType.newConfiguration("MyClone").add();
        NumericalRegisterSpec registerSpec = (NumericalRegisterSpec) ((NumericalRegisterSpecImpl) numericalRegisterSpec).cloneForDeviceConfig(cloneConfig);
        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(cloneConfig.getId());
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(deviceObisCode);
        assertThat(registerSpec.getObisCode()).isEqualTo(registerType.getObisCode());
        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(numberOfDigits);
        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(numberOfFractionDigits);
        assertThat(registerSpec.getOverflowValue()).isEqualTo(overFlowValue);
    }

    @Test
    @Transactional
    public void cloneNumericalRegisterWithoutOverruledObisCodeTest() {
        BigDecimal overFlowValue = BigDecimal.valueOf(65111L);
        int numberOfDigits = 7;
        int numberOfFractionDigits = 6;
        NumericalRegisterSpec.Builder builder = getReloadedDeviceConfiguration().createNumericalRegisterSpec(registerType);
        builder.setOverflowValue(overFlowValue);
        builder.setNumberOfDigits(numberOfDigits);
        builder.setNumberOfFractionDigits(numberOfFractionDigits);
        NumericalRegisterSpec numericalRegisterSpec = builder.add();

        DeviceConfiguration cloneConfig = deviceType.newConfiguration("MyClone").add();
        NumericalRegisterSpec registerSpec = (NumericalRegisterSpec) ((NumericalRegisterSpecImpl) numericalRegisterSpec).cloneForDeviceConfig(cloneConfig);
        assertThat(registerSpec.getDeviceConfiguration().getId()).isEqualTo(cloneConfig.getId());
        assertThat(registerSpec.getDeviceObisCode()).isEqualTo(registerType.getObisCode());
        assertThat(registerSpec.getObisCode()).isEqualTo(registerType.getObisCode());
        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(numberOfDigits);
        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(numberOfFractionDigits);
        assertThat(registerSpec.getOverflowValue()).isEqualTo(overFlowValue);
    }
}