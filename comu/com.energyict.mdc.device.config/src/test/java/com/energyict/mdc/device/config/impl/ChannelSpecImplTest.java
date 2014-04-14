package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateRegisterMappingException;
import com.energyict.mdc.device.config.exceptions.LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.RegisterMappingIsNotConfiguredException;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.math.BigDecimal;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.FlowDirection.REVERSE;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the {@link ChannelSpecImpl} component
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 15:48
 */
public class ChannelSpecImplTest extends DeviceTypeProvidingPersistenceTest {

    private static final String DEVICE_CONFIGURATION_NAME = ChannelSpecImplTest.class.getName() + "Config";
    private static final String LOAD_PROFILE_TYPE_NAME = ChannelSpecImplTest.class.getSimpleName() + "LoadProfileType";
    private static final String REGISTER_MAPPING_NAME = ChannelSpecImplTest.class.getSimpleName() + "RegisterMapping";
    private static final String DEFAULT_CHANNEL_SPEC_NAME = ChannelSpecImplTest.class.getName() + "ChannelSpec";

    private final ObisCode registerMappingObisCode = ObisCode.fromString("1.0.1.8.0.255");
    private final ObisCode loadProfileTypeObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final ObisCode overruledChannelSpecObisCode = ObisCode.fromString("1.0.1.8.2.255");

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private TimeDuration interval = TimeDuration.days(1);
    private DeviceConfiguration deviceConfiguration;
    private LoadProfileType loadProfileType;
    private RegisterMapping registerMapping;
    private Phenomenon phenomenon;
    private Unit phenomenonUnit = Unit.get("kWh");


    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithRegisterMappingAndLoadProfileTypeAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithRegisterMappingAndLoadProfileTypeAndDeviceConfiguration() {
        this.phenomenon = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon("BasicPhenomenon", phenomenonUnit);
        this.phenomenon.save();

        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        ReadingType readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(REGISTER_MAPPING_NAME, registerMappingObisCode, phenomenonUnit, readingType, readingType.getTou());
        this.registerMapping.save();
        loadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME, loadProfileTypeObisCode, interval);
        loadProfileType.addRegisterMapping(registerMapping);
        loadProfileType.save();


        // Business method
        deviceType.setDescription("For ChannelSpec Test purposes only");
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterMapping(registerMapping);

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
    }

    private LoadProfileSpec createDefaultTestingLoadProfileSpecWithOverruledObisCode() {
        LoadProfileSpec loadProfileSpec;
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfiguration.createLoadProfileSpec(this.loadProfileType);
        loadProfileSpecBuilder.setOverruledObisCode(overruledChannelSpecObisCode);
        loadProfileSpec = loadProfileSpecBuilder.add();
        return loadProfileSpec;
    }

    @Test
    @Transactional
    public void createChannelSpecTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        assertThat(channelSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(channelSpec.getObisCode()).isEqualTo(registerMappingObisCode);
        assertThat(channelSpec.getDeviceObisCode()).isEqualTo(registerMappingObisCode);
        assertThat(channelSpec.getInterval()).isEqualTo(interval);
        assertThat(channelSpec.getLoadProfileSpec()).isEqualTo(loadProfileSpec);
        assertThat(channelSpec.getName()).isEqualTo(REGISTER_MAPPING_NAME);
        assertThat(channelSpec.getRegisterMapping()).isEqualTo(this.registerMapping);
        assertThat(channelSpec.getPhenomenon()).isEqualTo(this.phenomenon);
    }

    private ChannelSpec createDefaultChannelSpec(LoadProfileSpec loadProfileSpec) {
        ChannelSpec channelSpec;
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpec = channelSpecBuilder.add();
        return channelSpec;
    }

    @Test
    @Transactional
    public void multiplierModeIsSetToCONFIGUREDONOBJECTByDefaultTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        assertThat(channelSpec.getMultiplierMode()).isEqualTo(MultiplierMode.CONFIGURED_ON_OBJECT);
    }

    @Test
    @Transactional
    public void multiplierIsSetToONEByDefaultTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        assertThat(channelSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void validateMultiplierIsChangeableWhenModeIsCONFIGUREDONOBJECTTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setMultiplier(BigDecimal.TEN);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getMultiplier()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @Transactional
    public void validateMultiplierIsSetBackToOneWhenModeIsNoneTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setMultiplier(BigDecimal.TEN);
        channelSpecBuilder.setMultiplierMode(MultiplierMode.NONE);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void validateMultiplierIsSetBackToOneWhenModeIsVERSIONEDTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setMultiplier(BigDecimal.TEN);
        channelSpecBuilder.setMultiplierMode(MultiplierMode.VERSIONED);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void numberOfFractionDigitsZeroByDefaultTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        assertThat(channelSpec.getNbrOfFractionDigits()).isEqualTo(0);
    }

    @Test
    @Transactional
    public void createWithNumberOfFractionDigitsTest() {
        ChannelSpec channelSpec;
        int digits = 3;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setNbrOfFractionDigits(digits);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getNbrOfFractionDigits()).isEqualTo(digits);
    }

    @Test
    @Transactional
    public void updateNumberOfFractionDigitsTest() {
        ChannelSpec channelSpec;
        int digits = 3;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = this.deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.setNbrOfFractionDigits(digits);
        channelSpecUpdater.update();

        assertThat(channelSpec.getNbrOfFractionDigits()).isEqualTo(digits);
    }

    @Test
    @Transactional
    public void overflowNullByDefaultTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        assertThat(channelSpec.getOverflow()).isNull();
    }

    @Test
    @Transactional
    public void createWithOverflowValueTest() {
        ChannelSpec channelSpec;
        BigDecimal overflow = BigDecimal.valueOf(10000000L);
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setOverflow(overflow);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getOverflow()).isEqualTo(overflow);
    }

    @Test
    @Transactional
    public void updateWithOverflowValueTest() {
        ChannelSpec channelSpec;
        BigDecimal overflow = BigDecimal.valueOf(10000000L);
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = this.deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.setOverflow(overflow);
        channelSpecUpdater.update();

        assertThat(channelSpec.getOverflow()).isEqualTo(overflow);
    }

    @Test
    @Transactional
    public void createWithOverruledObisCodeTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setOverruledObisCode(overruledChannelSpecObisCode);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getDeviceObisCode()).isEqualTo(overruledChannelSpecObisCode);
        assertThat(channelSpec.getObisCode()).isEqualTo(registerMappingObisCode);
    }

    @Test
    @Transactional
    public void updateWithOverruledObisCodeTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = this.deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.setOverruledObisCode(overruledChannelSpecObisCode);
        channelSpecUpdater.update();

        assertThat(channelSpec.getDeviceObisCode()).isEqualTo(overruledChannelSpecObisCode);
        assertThat(channelSpec.getObisCode()).isEqualTo(registerMappingObisCode);
    }

    @Test
    @Transactional
    public void validateReadingMethodIsByDefaultENGINEERINGUNITSTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        assertThat(channelSpec.getReadingMethod()).isEqualTo(ReadingMethod.ENGINEERING_UNIT);
    }

    @Test
    @Transactional
    public void createWithBASICDATAReadingMethodTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setReadingMethod(ReadingMethod.BASIC_DATA);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getReadingMethod()).isEqualTo(ReadingMethod.BASIC_DATA);
    }

    @Test
    @Transactional
    public void updateWithBASICDATAReadingMethodTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = this.deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.setReadingMethod(ReadingMethod.BASIC_DATA);
        channelSpecUpdater.update();

        assertThat(channelSpec.getReadingMethod()).isEqualTo(ReadingMethod.BASIC_DATA);
    }

    @Test
    @Transactional
    public void validateValueCalculationMethodIsByDefaultSetToAUTOMATICTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        assertThat(channelSpec.getValueCalculationMethod()).isEqualTo(ValueCalculationMethod.AUTOMATIC);
    }

    @Test
    @Transactional
    public void createWithRAWDATAValueCalculationMethodTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setValueCalculationMethod(ValueCalculationMethod.RAW_DATA);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getValueCalculationMethod()).isEqualTo(ValueCalculationMethod.RAW_DATA);
    }

    @Test
    @Transactional
    public void createWithFORCEMETERADVANCEValueCalculationMethodTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setValueCalculationMethod(ValueCalculationMethod.FORCE_METER_ADVANCE);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getValueCalculationMethod()).isEqualTo(ValueCalculationMethod.FORCE_METER_ADVANCE);
    }

    @Test
    @Transactional
    public void updateWithRAWDATAValueCalculationMethodTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = this.deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.setValueCalculationMethod(ValueCalculationMethod.RAW_DATA);
        channelSpecUpdater.update();

        assertThat(channelSpec.getValueCalculationMethod()).isEqualTo(ValueCalculationMethod.RAW_DATA);
    }

    @Test
    @Transactional
    public void updateWithFORCEMETERADVANCEValueCalculationMethodTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        channelSpec = createDefaultChannelSpec(loadProfileSpec);

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = this.deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.setValueCalculationMethod(ValueCalculationMethod.FORCE_METER_ADVANCE);
        channelSpecUpdater.update();

        assertThat(channelSpec.getValueCalculationMethod()).isEqualTo(ValueCalculationMethod.FORCE_METER_ADVANCE);
    }

    @Test(expected = RegisterMappingIsNotConfiguredException.class)
    @Transactional
    public void createWithRegisterMappingFromOtherDeviceTypeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        ReadingType readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();

        RegisterMapping registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(REGISTER_MAPPING_NAME + "Other", overruledChannelSpecObisCode, phenomenonUnit, readingType, readingType.getTou());
        registerMapping.save();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.add();
    }

    @Test(expected = RegisterMappingIsNotConfiguredException.class)
    @Transactional
    public void createWithRegisterMappingNotInLoadProfileTypeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        ReadingType readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();

        RegisterMapping registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(REGISTER_MAPPING_NAME + "Other", overruledChannelSpecObisCode, phenomenonUnit, readingType, readingType.getTou());
        registerMapping.save();
        this.deviceType.addRegisterMapping(registerMapping);
        this.deviceType.save();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.add();
    }

    @Test(expected = LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException.class)
    @Transactional
    public void createWithLoadProfileSpecFromOtherConfigTest() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME + "Other");
        DeviceConfiguration otherDeviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();

        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = otherDeviceConfiguration.createLoadProfileSpec(this.loadProfileType);
        loadProfileSpecBuilder.setOverruledObisCode(overruledChannelSpecObisCode);
        LoadProfileSpec otherLoadProfileSpec = loadProfileSpecBuilder.add();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, otherLoadProfileSpec);
        channelSpecBuilder.add();
    }

    @Test(expected = DuplicateRegisterMappingException.class)
    @Transactional
    public void createWithSameRegisterMappingTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        createDefaultChannelSpec(loadProfileSpec);
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setName(DEFAULT_CHANNEL_SPEC_NAME);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CHANNEL_SPEC_VALUE_CALCULATION_METHOD_IS_REQUIRED_KEY + "}")
    public void createWithoutValueCalculationMethodTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setValueCalculationMethod(null);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CHANNEL_SPEC_MULTIPLIER_MODE_IS_REQUIRED_KEY + "}")
    public void createWithoutMultiplierModeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setMultiplierMode(null);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CHANNEL_SPEC_MULTIPLIER_IS_REQUIRED_WHEN_KEY + "}")
    public void createWithoutMultiplierTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
        channelSpecBuilder.setMultiplier(null);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    public void successfulDeleteTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec channelSpec = createDefaultChannelSpec(loadProfileSpec);
        this.deviceConfiguration.deleteChannelSpec(channelSpec);

        assertThat(this.deviceConfiguration.getChannelSpecs()).hasSize(0);
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    @Transactional
    public void deleteFromActiveDeviceConfigurationTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec channelSpec = createDefaultChannelSpec(loadProfileSpec);
        this.deviceConfiguration.activate();
        this.deviceConfiguration.deleteChannelSpec(channelSpec);
    }
}
