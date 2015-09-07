package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateChannelTypeException;
import com.energyict.mdc.device.config.exceptions.LoadProfileSpecIsNotConfiguredOnDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.RegisterTypeIsNotConfiguredException;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import org.junit.*;
import org.junit.rules.*;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.FlowDirection.REVERSE;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the {@link ChannelSpecImpl} component.
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 15:48
 */
public class ChannelSpecImplTest extends DeviceTypeProvidingPersistenceTest {

    private static final String DEVICE_CONFIGURATION_NAME = ChannelSpecImplTest.class.getName() + "Config";
    private static final String LOAD_PROFILE_TYPE_NAME = ChannelSpecImplTest.class.getSimpleName() + "LoadProfileType";

    private final ObisCode channelTypeObisCode = ObisCode.fromString("1.0.1.8.0.255");
    private final ObisCode loadProfileTypeObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final ObisCode overruledChannelSpecObisCode = ObisCode.fromString("1.0.1.8.2.255");

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private TimeDuration interval = TimeDuration.days(1);
    private DeviceConfiguration deviceConfiguration;
    private LoadProfileType loadProfileType;
    private RegisterType registerType;
    private Unit unit = Unit.get("kWh");
    private ChannelType channelType;

    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithRegisterTypeAndLoadProfileTypeAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithRegisterTypeAndLoadProfileTypeAndDeviceConfiguration() {

        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.BULKQUANTITY).code();
        ReadingType readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService()
                    .findRegisterTypeByReadingType(readingType);
        if (xRegisterType.isPresent()) {
            this.registerType = xRegisterType.get();
        }
        else {
            this.registerType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, channelTypeObisCode);
            this.registerType.save();
        }
        loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME, loadProfileTypeObisCode, interval, Arrays.asList(registerType));
        channelType = loadProfileType.findChannelType(registerType).get();
        loadProfileType.save();

        // Business method
        deviceType.setDescription("For ChannelSpec Test purposes only");
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType);

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
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
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
        channelSpec = channelSpecBuilder.add();
        return channelSpec;
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

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
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

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
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

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
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

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.setOverflow(overflow);
        channelSpecUpdater.update();

        assertThat(channelSpec.getOverflow()).isEqualTo(overflow);
    }

    @Test
    @Transactional
    public void createWithOverruledObisCodeTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
        channelSpecBuilder.setOverruledObisCode(overruledChannelSpecObisCode);
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
        channelSpecUpdater.setOverruledObisCode(overruledChannelSpecObisCode);
        channelSpecUpdater.update();

        assertThat(channelSpec.getDeviceObisCode()).isEqualTo(overruledChannelSpecObisCode);
        assertThat(channelSpec.getObisCode()).isEqualTo(channelTypeObisCode);
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

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
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

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
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

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
        channelSpecBuilder.setValueCalculationMethod(ValueCalculationMethod.RAW_DATA);
        channelSpec = channelSpecBuilder.add();

        assertThat(channelSpec.getValueCalculationMethod()).isEqualTo(ValueCalculationMethod.RAW_DATA);
    }

    @Test
    @Transactional
    public void createWithFORCEMETERADVANCEValueCalculationMethodTest() {
        ChannelSpec channelSpec;
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
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

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
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

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = getReloadedDeviceConfiguration().getChannelSpecUpdaterFor(channelSpec);
        channelSpecUpdater.setValueCalculationMethod(ValueCalculationMethod.FORCE_METER_ADVANCE);
        channelSpecUpdater.update();

        assertThat(channelSpec.getValueCalculationMethod()).isEqualTo(ValueCalculationMethod.FORCE_METER_ADVANCE);
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
        this.deviceType.save();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(otherChannelType, loadProfileSpec);
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

        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, otherLoadProfileSpec);
        channelSpecBuilder.add();
    }

    @Test(expected = DuplicateChannelTypeException.class)
    @Transactional
    public void createWithSameChannelTypeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        createDefaultChannelSpec(loadProfileSpec);
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
        channelSpecBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CHANNEL_SPEC_VALUE_CALCULATION_METHOD_IS_REQUIRED + "}")
    public void createWithoutValueCalculationMethodTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec);
        channelSpecBuilder.setValueCalculationMethod(null);
        channelSpecBuilder.add();
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
        assertThat(clonedChannelSpec.getOverflow()).isEqualTo(channelSpec.getOverflow());
        assertThat(clonedChannelSpec.getReadingMethod().getCode()).isEqualTo(channelSpec.getReadingMethod().getCode());
        assertThat(clonedChannelSpec.getValueCalculationMethod().getCode()).isEqualTo(channelSpec.getValueCalculationMethod().getCode());
    }

    @Test
    @Transactional
    public void cloneChannelSpecsWithOverruledObisCodeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        ObisCode deviceChannelObisCode = ObisCode.fromString("1.2.3.4.55.6");
        ChannelSpec channelSpec = getReloadedDeviceConfiguration().createChannelSpec(channelType, loadProfileSpec).setOverruledObisCode(deviceChannelObisCode).add();
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
        assertThat(clonedChannelSpec.getOverflow()).isEqualTo(channelSpec.getOverflow());
        assertThat(clonedChannelSpec.getReadingMethod().getCode()).isEqualTo(channelSpec.getReadingMethod().getCode());
        assertThat(clonedChannelSpec.getValueCalculationMethod().getCode()).isEqualTo(channelSpec.getValueCalculationMethod().getCode());
    }
}
