package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenMeasurementTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdatePhenomenonWhenMeasurementTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.base.Optional;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the peristence aspects of the {@link com.energyict.mdc.masterdata.MeasurementType} component
 * that impact the {@link DeviceConfigurationServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class MeasurementTypeImplTest extends PersistenceTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.MINUTES);

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private LoadProfileType loadProfileType;
    private ReadingType readingType1;
    private ReadingType readingType2;
    private Phenomenon phenomenon1;
    private Phenomenon phenomenon2;
    private ObisCode obisCode1;
    private ObisCode obisCode2;
    private Unit unit1;
    private Unit unit2;

    @Before
    public void registerEventHandlers () {
        inMemoryPersistence.registerEventHandlers();
    }

    @After
    public void unregisterEventHandlers () {
        inMemoryPersistence.unregisterEventHandlers();
    }

    @Test(expected = CannotUpdateObisCodeWhenMeasurementTypeIsInUseException.class)
    @Transactional
    public void testCannotUpdateObisCodeWhenUsedByRegisterSpec() {
        String registerTypeName = "testCannotUpdateObisCodeWhenUsedByRegisterSpec";
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
            registerType.setDescription("For testing purposes only");
            registerType.save();
        }

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerTypeName, this.deviceProtocolPluggableClass);
        deviceType.addRegisterType(registerType);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        NumericalRegisterSpec.Builder registerSpecBuilder = configurationBuilder.newNumericalRegisterSpec(registerType);
        registerSpecBuilder.setNumberOfDigits(5);
        registerSpecBuilder.setNumberOfFractionDigits(2);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerType.setObisCode(ObisCode.fromString("1.0.3.9.0.255"));
        registerType.save();

        // Asserts: expected CannotUpdateObisCodeWhenRegisterTypeIsInUseException
    }

    @Test(expected = CannotUpdateObisCodeWhenMeasurementTypeIsInUseException.class)
    @Transactional
    public void testCannotUpdateObisCodeWhenUsedByChannelSpec() {
        String registerTypeName = "testCannotUpdateObisCodeWhenUsedByChannelSpec";
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
            registerType.setDescription("For testing purposes only");
            registerType.save();
        }

        this.setupLoadProfileTypesInExistingTransaction();

        ChannelType channelTypeForRegisterType = this.loadProfileType.createChannelTypeForRegisterType(registerType);
        this.loadProfileType.save();

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerTypeName, this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(this.loadProfileType);
        deviceType.addRegisterType(registerType);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
        configurationBuilder.newChannelSpec(channelTypeForRegisterType, this.phenomenon1, loadProfileSpecBuilder).setReadingMethod(ReadingMethod.BASIC_DATA).setMultiplierMode(MultiplierMode.NONE).setValueCalculationMethod(ValueCalculationMethod.AUTOMATIC);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerType.setObisCode(ObisCode.fromString("1.0.3.9.0.255"));
        registerType.save();

        // Asserts: expected CannotUpdateObisCodeWhenRegisterTypeIsInUseException
    }

    @Test(expected = CannotUpdatePhenomenonWhenMeasurementTypeIsInUseException.class)
    @Transactional
    public void testCannotUpdateUnitWhenUsedByRegisterSpec() {
        String registerTypeName = "testCannotUpdateUnitWhenUsedByRegisterSpec";
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
            registerType.setDescription("For testing purposes only");
            registerType.save();
        }

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerTypeName, this.deviceProtocolPluggableClass);
        deviceType.addRegisterType(registerType);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        NumericalRegisterSpec.Builder registerSpecBuilder = configurationBuilder.newNumericalRegisterSpec(registerType);
        registerSpecBuilder.setNumberOfDigits(5);
        registerSpecBuilder.setNumberOfFractionDigits(2);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerType.setUnit(unit2);
        registerType.save();

        // Asserts: expected CannotUpdateProductSpecWhenRegisterTypeIsInUseException
    }

    @Test(expected = CannotUpdatePhenomenonWhenMeasurementTypeIsInUseException.class)
    @Transactional
    public void testCannotUpdatePhenomenonWhenUsedByChannelSpec() {
        String registerTypeName = "testCannotUpdateProductSpecWhenUsedByChannelSpec";
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
            registerType.setDescription("For testing purposes only");
            registerType.save();
        }

        this.setupLoadProfileTypesInExistingTransaction();

        ChannelType channelTypeForRegisterType = this.loadProfileType.createChannelTypeForRegisterType(registerType);
        this.loadProfileType.save();

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerTypeName, this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(this.loadProfileType);
        deviceType.addRegisterType(registerType);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
        configurationBuilder.newChannelSpec(channelTypeForRegisterType, this.phenomenon1, loadProfileSpecBuilder).setReadingMethod(ReadingMethod.BASIC_DATA).setMultiplierMode(MultiplierMode.NONE).setValueCalculationMethod(ValueCalculationMethod.AUTOMATIC);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerType.setUnit(unit2);
        registerType.save();

        // Asserts: expected CannotUpdateProductSpecWhenRegisterTypeIsInUseException
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCannotDeleteWhenUsedByRegisterSpecs() {
        String registerTypeName = "testCannotDeleteWhenUsedByRegisterSpecs";
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
            registerType.setDescription("For testing purposes only");
            registerType.save();
        }

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerTypeName, this.deviceProtocolPluggableClass);
        deviceType.addRegisterType(registerType);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        NumericalRegisterSpec.Builder registerSpecBuilder = configurationBuilder.newNumericalRegisterSpec(registerType);
        registerSpecBuilder.setNumberOfDigits(5);
        registerSpecBuilder.setNumberOfFractionDigits(2);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        configurationBuilder.add();
        deviceType.save();

        try {
            registerType.delete();
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_TYPE_STILL_USED_BY_REGISTER_SPEC);
            throw e;
        }
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCannotDeleteWhenUsedByDeviceType() {
        String registerTypeName = "testCannotDeleteWhenUsedByDeviceType";
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
            registerType.setDescription("For testing purposes only");
            registerType.save();
        }

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerTypeName, this.deviceProtocolPluggableClass);
        deviceType.addRegisterType(registerType);
        deviceType.save();

        try {
            registerType.delete();
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_TYPE_STILL_USED_BY_DEVICE_TYPE);
            throw e;
        }
    }

    @Test
    @Transactional
    public void deleteChannelTypesWhenRegisterTypesAreDeletedTest() {
        String registerTypeName = "deleteChannelTypesWhenRegisterTypesAreDeletedTest";
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
            registerType.setDescription("For testing purposes only");
            registerType.save();
        }

        this.setupLoadProfileTypesInExistingTransaction();

        ChannelType channelTypeForRegisterType = this.loadProfileType.createChannelTypeForRegisterType(registerType);
        this.loadProfileType.save();

        this.loadProfileType.delete();
        registerType.delete();

        assertThat(inMemoryPersistence.getMasterDataService().findAllChannelTypes().find()).isEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{duplicate.channelType.interval.registerType}")
    public void duplicateChannelTypeTest() {
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType("duplicateChannelTypeTest", obisCode1, unit1, readingType1, 1);
            registerType.setDescription("For testing purposes only");
            registerType.save();
        }

        TimeDuration fifteenMinutes = TimeDuration.minutes(15);
        ReadingType intervalAppliedReadingType1 = inMemoryPersistence.getReadingTypeUtilService().getIntervalAppliedReadingType(readingType1, fifteenMinutes, obisCode1);
        ReadingType intervalAppliedReadingType2 = inMemoryPersistence.getReadingTypeUtilService().getIntervalAppliedReadingType(readingType2, fifteenMinutes, obisCode2);
        ChannelType firstChannelType = inMemoryPersistence.getMasterDataService().newChannelType(registerType, fifteenMinutes, intervalAppliedReadingType1);
        firstChannelType.save();
        ChannelType secondChannelType = inMemoryPersistence.getMasterDataService().newChannelType(registerType, fifteenMinutes, intervalAppliedReadingType2);
        secondChannelType.save();
    }

    @Test
    @Transactional
    public void loadProfileTypesReuseChannelTypesTest() {
        String registerTypeName = "loadProfileTypesReuseChannelTypesTest";
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
            registerType.setDescription("For testing purposes only");
            registerType.save();
        }

        this.setupLoadProfileTypesInExistingTransaction();

        ChannelType channelTypeForRegisterType = this.loadProfileType.createChannelTypeForRegisterType(registerType);
        this.loadProfileType.save();

        LoadProfileType loadProfileType2 = inMemoryPersistence.getMasterDataService().newLoadProfileType("LoadProfileTest2", ObisCode.fromString("1.0.99.2.0.255"), INTERVAL_15_MINUTES);
        loadProfileType2.save();

        ChannelType shouldBeSameChannelType = loadProfileType2.createChannelTypeForRegisterType(registerType);

        assertThat(channelTypeForRegisterType.getId()).isEqualTo(shouldBeSameChannelType.getId());
        assertThat(inMemoryPersistence.getMasterDataService().findAllChannelTypes().find()).hasSize(1);
    }

    private void setupProductSpecsInExistingTransaction() {
        this.setupReadingTypesInExistingTransaction();
        this.setupPhenomenaInExistingTransaction();
    }

    private void setupReadingTypesInExistingTransaction() {
        String code = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.obisCode1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(readingType1).getObisCode();
        String code2 = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        this.obisCode2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(readingType2).getObisCode();
    }

    private void setupLoadProfileTypesInExistingTransaction() {
        this.loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(MeasurementTypeImplTest.class.getSimpleName(), ObisCode.fromString("1.0.99.1.0.255"), INTERVAL_15_MINUTES);
        this.loadProfileType.save();
    }

    private void setupPhenomenaInExistingTransaction() {
        this.unit1 = Unit.get("kWh");
        this.phenomenon1 = createPhenomenonIfMissing(this.unit1, MeasurementTypeImplTest.class.getSimpleName() + "1");
        this.unit2 = Unit.get("MWh");
        this.phenomenon2 = createPhenomenonIfMissing(this.unit2, MeasurementTypeImplTest.class.getSimpleName() + "2");
    }

    private Phenomenon createPhenomenonIfMissing(Unit unit, String name) {
        Optional<Phenomenon> phenomenonByUnit = inMemoryPersistence.getMasterDataService().findPhenomenonByUnit(unit);
        if (!phenomenonByUnit.isPresent()) {
            Phenomenon phenomenon = inMemoryPersistence.getMasterDataService().newPhenomenon(name, unit);
            phenomenon.save();
            return phenomenon;
        }
        else {
            return phenomenonByUnit.get();
        }
    }

}