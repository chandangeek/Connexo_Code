package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdatePhenomenonWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;
import com.google.common.base.Optional;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the peristence aspects of the {@link RegisterMapping} component
 * that impact the {@link DeviceConfigurationServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterMappingImplTest extends PersistenceTest {

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

    @Test(expected = CannotUpdateObisCodeWhenRegisterMappingIsInUseException.class)
    @Transactional
    public void testCannotUpdateObisCodeWhenUsedByRegisterSpec() {
        String registerMappingName = "testCannotUpdateObisCodeWhenUsedByRegisterSpec";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        Optional<RegisterMapping> xRegisterMapping =
                inMemoryPersistence.getMasterDataService().
                        findRegisterMappingByObisCodeAndUnitAndTimeOfUse(
                                obisCode1,
                                unit1,
                                readingType1.getTou());
        if (xRegisterMapping.isPresent()) {
            registerMapping = xRegisterMapping.get();
        }
        else {
            registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, 1);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
        }

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
        deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = configurationBuilder.newRegisterSpec(registerMapping);
        registerSpecBuilder.setNumberOfDigits(5);
        registerSpecBuilder.setNumberOfFractionDigits(2);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerMapping.setObisCode(ObisCode.fromString("1.0.3.9.0.255"));
        registerMapping.save();

        // Asserts: expected CannotUpdateObisCodeWhenRegisterMappingIsInUseException
    }

    @Test(expected = CannotUpdateObisCodeWhenRegisterMappingIsInUseException.class)
    @Transactional
    public void testCannotUpdateObisCodeWhenUsedByChannelSpec() {
        String registerMappingName = "testCannotUpdateObisCodeWhenUsedByChannelSpec";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        Optional<RegisterMapping> xRegisterMapping =
                inMemoryPersistence.getMasterDataService().
                        findRegisterMappingByObisCodeAndUnitAndTimeOfUse(
                                obisCode1,
                                unit1,
                                readingType1.getTou());
        if (xRegisterMapping.isPresent()) {
            registerMapping = xRegisterMapping.get();
        }
        else {
            registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, 1);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
        }

        this.setupLoadProfileTypesInExistingTransaction();

        this.loadProfileType.addRegisterMapping(registerMapping);
        this.loadProfileType.save();

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(this.loadProfileType);
        deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
        configurationBuilder.newChannelSpec(registerMapping, this.phenomenon1, loadProfileSpecBuilder).setReadingMethod(ReadingMethod.BASIC_DATA).setMultiplierMode(MultiplierMode.NONE).setValueCalculationMethod(ValueCalculationMethod.AUTOMATIC);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerMapping.setObisCode(ObisCode.fromString("1.0.3.9.0.255"));
        registerMapping.save();

        // Asserts: expected CannotUpdateObisCodeWhenRegisterMappingIsInUseException
    }

    @Test(expected = CannotUpdatePhenomenonWhenRegisterMappingIsInUseException.class)
    @Transactional
    public void testCannotUpdateUnitWhenUsedByRegisterSpec() {
        String registerMappingName = "testCannotUpdateUnitWhenUsedByRegisterSpec";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        Optional<RegisterMapping> xRegisterMapping =
                inMemoryPersistence.getMasterDataService().
                        findRegisterMappingByObisCodeAndUnitAndTimeOfUse(
                                obisCode1,
                                unit1,
                                readingType1.getTou());
        if (xRegisterMapping.isPresent()) {
            registerMapping = xRegisterMapping.get();
        }
        else {
            registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, 1);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
        }

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
        deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = configurationBuilder.newRegisterSpec(registerMapping);
        registerSpecBuilder.setNumberOfDigits(5);
        registerSpecBuilder.setNumberOfFractionDigits(2);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerMapping.setUnit(unit2);
        registerMapping.save();

        // Asserts: expected CannotUpdateProductSpecWhenRegisterMappingIsInUseException
    }

    @Test(expected = CannotUpdatePhenomenonWhenRegisterMappingIsInUseException.class)
    @Transactional
    public void testCannotUpdatePhenomenonWhenUsedByChannelSpec() {
        String registerMappingName = "testCannotUpdateProductSpecWhenUsedByChannelSpec";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        Optional<RegisterMapping> xRegisterMapping =
                inMemoryPersistence.getMasterDataService().
                        findRegisterMappingByObisCodeAndUnitAndTimeOfUse(
                                obisCode1,
                                unit1,
                                readingType1.getTou());
        if (xRegisterMapping.isPresent()) {
            registerMapping = xRegisterMapping.get();
        }
        else {
            registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, 1);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
        }

        this.setupLoadProfileTypesInExistingTransaction();

        this.loadProfileType.addRegisterMapping(registerMapping);
        this.loadProfileType.save();

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(this.loadProfileType);
        deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
        configurationBuilder.newChannelSpec(registerMapping, this.phenomenon1, loadProfileSpecBuilder).setReadingMethod(ReadingMethod.BASIC_DATA).setMultiplierMode(MultiplierMode.NONE).setValueCalculationMethod(ValueCalculationMethod.AUTOMATIC);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerMapping.setUnit(unit2);
        registerMapping.save();

        // Asserts: expected CannotUpdateProductSpecWhenRegisterMappingIsInUseException
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCannotDeleteWhenUsedByRegisterSpecs() {
        String registerMappingName = "testCannotDeleteWhenUsedByRegisterSpecs";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        Optional<RegisterMapping> xRegisterMapping =
                inMemoryPersistence.getMasterDataService().
                        findRegisterMappingByObisCodeAndUnitAndTimeOfUse(
                                obisCode1,
                                unit1,
                                readingType1.getTou());
        if (xRegisterMapping.isPresent()) {
            registerMapping = xRegisterMapping.get();
        }
        else {
            registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, 1);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
        }

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
        deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = configurationBuilder.newRegisterSpec(registerMapping);
        registerSpecBuilder.setNumberOfDigits(5);
        registerSpecBuilder.setNumberOfFractionDigits(2);
        registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        configurationBuilder.add();
        deviceType.save();

        try {
            registerMapping.delete();
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_REGISTER_SPEC);
            throw e;
        }
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCannotDeleteWhenUsedByDeviceType() {
        String registerMappingName = "testCannotDeleteWhenUsedByDeviceType";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        Optional<RegisterMapping> xRegisterMapping =
                inMemoryPersistence.getMasterDataService().
                        findRegisterMappingByObisCodeAndUnitAndTimeOfUse(
                                obisCode1,
                                unit1,
                                readingType1.getTou());
        if (xRegisterMapping.isPresent()) {
            registerMapping = xRegisterMapping.get();
        }
        else {
            registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, 1);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
        }

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
        deviceType.addRegisterMapping(registerMapping);
        deviceType.save();

        try {
            registerMapping.delete();
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_DEVICE_TYPE);
            throw e;
        }
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
        this.loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(RegisterMappingImplTest.class.getSimpleName(), ObisCode.fromString("1.0.99.1.0.255"), INTERVAL_15_MINUTES);
        this.loadProfileType.save();
    }

    private void setupPhenomenaInExistingTransaction() {
        this.unit1 = Unit.get("kWh");
        this.phenomenon1 = createPhenomenonIfMissing(this.unit1, RegisterMappingImplTest.class.getSimpleName() + "1");
        this.unit2 = Unit.get("MWh");
        this.phenomenon2 = createPhenomenonIfMissing(this.unit2, RegisterMappingImplTest.class.getSimpleName() + "2");
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