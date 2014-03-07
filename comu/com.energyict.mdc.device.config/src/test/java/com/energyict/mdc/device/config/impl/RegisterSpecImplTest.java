package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ChannelSpecLinkType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.DuplicatePrimeRegisterSpecException;
import com.energyict.mdc.device.config.exceptions.InvalidValueException;
import com.energyict.mdc.device.config.exceptions.OverFlowValueCanNotExceedNumberOfDigitsException;
import com.energyict.mdc.device.config.exceptions.OverFlowValueHasIncorrectFractionDigitsException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
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

    private TimeDuration interval = TimeDuration.days(1);

    private DeviceConfiguration deviceConfiguration;
    private RegisterMapping registerMapping;
    private ReadingType readingType;
    private ProductSpec productSpec;

    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithRegisterSpecAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithRegisterSpecAndDeviceConfiguration() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(readingType);
        this.productSpec.save();
        this.registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(REGISTER_MAPPING_NAME, registerMappingObisCode, productSpec);
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
        assertThat(registerSpec.getChannelSpecLinkType()).isNull();
        assertThat(registerSpec.getLinkedChannelSpec()).isNull();
        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(this.numberOfDigits);
        assertThat(registerSpec.getNumberOfFractionDigits()).isEqualTo(this.numberOfFractionDigits);
        assertThat(registerSpec.getOverflowValue()).isNull();
        assertThat(registerSpec.getMultiplier()).isEqualTo(BigDecimal.ONE);
        assertThat(registerSpec.getMultiplierMode()).isEqualTo(MultiplierMode.NONE);
    }

    @Test
    @Transactional
    public void updateNumberOfDigitsRegisterSpecTest() {
        RegisterSpec registerSpec = createDefaultRegisterSpec();
        int updatedNumberOfDigits = 98;

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec);
        registerSpecUpdater.setNumberOfDigits(updatedNumberOfDigits);
        registerSpecUpdater.update();

        assertThat(registerSpec.getNumberOfDigits()).isEqualTo(updatedNumberOfDigits);
    }

    @Test(expected = InvalidValueException.class)
    @Transactional
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
        RegisterMapping otherMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("OtherMapping", ObisCode.fromString("1.2.3.1.5.6"), this.productSpec);
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

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDifferentMappingButSameObisCodeTest() {
        RegisterSpec registerSpec1 = createDefaultRegisterSpec();
        RegisterSpec registerSpec2;
        RegisterMapping otherMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("OtherMapping", registerMappingObisCode, this.productSpec);
        otherMapping.save();
        this.deviceType.addRegisterMapping(otherMapping);
        this.deviceType.save();
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(otherMapping);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpec2 = registerSpecBuilder.add();
    }

    @Test(expected = RegisterMappingIsNotConfiguredOnDeviceTypeException.class)
    @Transactional
    public void addSpecForMappingWhichIsNotOnDeviceTypeTest() {
        RegisterSpec registerSpec;
        RegisterMapping otherMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("OtherMapping", ObisCode.fromString("32.12.32.5.12.32"), this.productSpec);
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
    public void updateWithChannelSpecTest() {
        ChannelSpec channelSpec;
        Phenomenon phenomenon = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon("BasicPhenomenon", Unit.get("kWh"));
        phenomenon.save();
        LoadProfileType loadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType("LoadProfileType", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.days(1));
        loadProfileType.addRegisterMapping(registerMapping);
        loadProfileType.save();
        this.deviceType.addLoadProfileType(loadProfileType);
        this.deviceType.save();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpecBuilder.add());
        channelSpec = channelSpecBuilder.add();

        RegisterSpec defaultRegisterSpec;
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpecBuilder.setLinkedChannelSpec(channelSpec);
        registerSpecBuilder.setChannelSpecLinkType(ChannelSpecLinkType.PRIME);
        defaultRegisterSpec = registerSpecBuilder.add();

        RegisterSpec loaded = inMemoryPersistence.getDeviceConfigurationService().findRegisterSpec(defaultRegisterSpec.getId());

        assertThat(loaded.getLinkedChannelSpec().getId()).isEqualTo(channelSpec.getId());
        assertThat(loaded.getChannelSpecLinkType()).isEqualTo(ChannelSpecLinkType.PRIME);
    }

    @Test(expected = DuplicatePrimeRegisterSpecException.class)
    @Transactional
    public void cannotCreateDoublePrimeRegisterForChannelTest() {
        RegisterSpec defaultRegisterSpec = createDefaultRegisterSpec();
        ChannelSpec channelSpec;
        Phenomenon phenomenon = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon("BasicPhenomenon", Unit.get("kWh"));
        phenomenon.save();
        LoadProfileType loadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType("LoadProfileType", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.days(1));
        loadProfileType.addRegisterMapping(registerMapping);
        loadProfileType.save();
        this.deviceType.addLoadProfileType(loadProfileType);
        try {
            this.deviceType.save();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpecBuilder.add());
        channelSpec = channelSpecBuilder.add();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(defaultRegisterSpec);
        registerSpecUpdater.setLinkedChannelSpec(channelSpec);
        registerSpecUpdater.setChannelSpecLinkType(ChannelSpecLinkType.PRIME);
        registerSpecUpdater.update();

        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpecBuilder.setOverruledObisCode(ObisCode.fromString("1.1.1.1.1.1"));
        registerSpecBuilder.setLinkedChannelSpec(channelSpec);
        registerSpecBuilder.setChannelSpecLinkType(ChannelSpecLinkType.PRIME);

        RegisterSpec registerSpec2 = registerSpecBuilder.add();
    }

    @Test(expected = DuplicatePrimeRegisterSpecException.class)
    @Transactional
    public void cannotUpdateDoublePrimeRegisterForChannelTest() {
        RegisterSpec defaultRegisterSpec = createDefaultRegisterSpec();
        ChannelSpec channelSpec;
        Phenomenon phenomenon = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon("BasicPhenomenon", Unit.get("kWh"));
        phenomenon.save();
        LoadProfileType loadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType("LoadProfileType", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.days(1));
        loadProfileType.addRegisterMapping(registerMapping);
        loadProfileType.save();
        this.deviceType.addLoadProfileType(loadProfileType);
        this.deviceType.save();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = this.deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpecBuilder.add());
        channelSpec = channelSpecBuilder.add();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater = this.deviceConfiguration.getRegisterSpecUpdaterFor(defaultRegisterSpec);
        registerSpecUpdater.setLinkedChannelSpec(channelSpec);
        registerSpecUpdater.setChannelSpecLinkType(ChannelSpecLinkType.PRIME);
        registerSpecUpdater.update();

        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = this.deviceConfiguration.createRegisterSpec(registerMapping);
        setRegisterSpecDefaultFields(registerSpecBuilder);
        registerSpecBuilder.setOverruledObisCode(ObisCode.fromString("1.1.1.1.1.1"));
        registerSpecBuilder.setLinkedChannelSpec(channelSpec);
        registerSpecBuilder.setChannelSpecLinkType(ChannelSpecLinkType.TIME_OF_USE);
        RegisterSpec registerSpec2 = registerSpecBuilder.add();

        RegisterSpec.RegisterSpecUpdater registerSpecUpdater2 = this.deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec2);
        registerSpecUpdater2.setChannelSpecLinkType(ChannelSpecLinkType.PRIME);
        registerSpecUpdater2.update();
    }
}