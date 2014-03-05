package com.energyict.mdc.device.config.impl;

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
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateProductSpecWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the peristence aspects of the {@link RegisterMappingImpl} component
 * as provided by the {@link DeviceConfigurationServiceImpl}.
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
    private ReadingType readingType;
    private ReadingType readingType2;
    private ProductSpec productSpec;
    private ProductSpec productSpec2;
    private Phenomenon phenomenon;
    private ObisCode obisCode1;
    private ObisCode obisCode2;

    @Test
    @Transactional
    public void testCreateWithoutViolations() {
        String registerMappingName = "testCreateWithoutViolations";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Business method
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Asserts
        assertThat(registerMapping).isNotNull();
        assertThat(registerMapping.getId()).isGreaterThan(0);
        assertThat(registerMapping.getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping.getDescription()).isNotEmpty();
        assertThat(registerMapping.getReadingType()).isEqualTo(this.readingType);
        assertThat(registerMapping.getObisCode()).isEqualTo(obisCode1);
    }

    @Test
    @Transactional
    public void testFindAfterCreation() {
        String registerMappingName = "testFindAfterCreation";
        this.setupProductSpecsInExistingTransaction();

        RegisterMapping registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Business method
        RegisterMapping registerMapping2 = inMemoryPersistence.getDeviceConfigurationService().findRegisterMappingByObisCodeAndProductSpec(obisCode1, this.productSpec);

        // Asserts
        assertThat(registerMapping2).isNotNull();
        assertThat(registerMapping2.getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping2.getDescription()).isNotEmpty();
        assertThat(registerMapping2.getReadingType()).isEqualTo(this.readingType);
        assertThat(registerMapping2.getObisCode()).isEqualTo(obisCode1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    public void testCreateWithoutName() {
        this.setupProductSpecsInExistingTransaction();

        RegisterMapping registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(null, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");

        // Business method
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    public void testCreateWithEmptyName() {
        this.setupProductSpecsInExistingTransaction();

        RegisterMapping registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("", obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");

        // Business method
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED_KEY + "}")
    public void testCreateWithoutObisCode() {
        String registerMappingName = "testCreateWithoutObisCode";
        this.setupProductSpecsInExistingTransaction();

        // Business method
        RegisterMapping registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, null, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PRODUCT_SPEC_IS_REQUIRED_KEY + "}")
    public void testCreateWithoutProductSpec() {
        setupProductSpecsInExistingTransaction();
        String registerMappingName = "testCreateWithoutProductSpec";
        RegisterMapping registerMapping;
        // Business method
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, null);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testUpdateName() {
        String registerMappingName = "testUpdateObisCode";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Business method
        String updatedName = registerMappingName + "-Updated";
        registerMapping.setName(updatedName);
        registerMapping.save();

        // Asserts
        assertThat(registerMapping.getName()).isEqualTo(updatedName);
    }

    @Test
    @Transactional
    public void testUpdateObisCodeAndProductSpec() {
        String registerMappingName = "testUpdateObisCode";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Business method
        registerMapping.setObisCode(obisCode2);
        registerMapping.setProductSpec(productSpec2);
        registerMapping.save();

        // Asserts
        assertThat(registerMapping.getObisCode()).isEqualTo(obisCode2);
        assertThat(registerMapping.getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping.getDescription()).isNotEmpty();
        assertThat(registerMapping.getReadingType()).isEqualTo(this.readingType2);
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void testUpdateObisCodeWithDuplicate() {
        String registerMappingName = "testUpdateObisCodeWithDuplicate";
        RegisterMapping updateCandidate;
        this.setupProductSpecsInExistingTransaction();

        updateCandidate = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        updateCandidate.setDescription("For testing purposes only");
        updateCandidate.save();

        RegisterMapping other = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("other", obisCode2, this.productSpec);
        other.save();

        try {
            // Business method
            updateCandidate.setObisCode(obisCode2);
            updateCandidate.save();
        } catch (DuplicateObisCodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_OBIS_CODE_ALREADY_EXISTS);
            throw e;
        }
    }

    @Test(expected = CannotUpdateObisCodeWhenRegisterMappingIsInUseException.class)
    @Transactional
    public void testCannotUpdateObisCodeWhenUsedByRegisterSpec() {
        String registerMappingName = "testCannotUpdateObisCodeWhenUsedByRegisterSpec";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

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
        registerMapping.setObisCode(obisCode2);
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
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        this.setupPhenomenaInExistingTransaction();
        this.setupLoadProfileTypesInExistingTransaction();

        this.loadProfileType.addRegisterMapping(registerMapping);
        this.loadProfileType.save();

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(this.loadProfileType);
        deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
        configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder).setReadingMethod(ReadingMethod.BASIC_DATA).setMultiplierMode(MultiplierMode.NONE).setValueCalculationMethod(ValueCalculationMethod.AUTOMATIC);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerMapping.setObisCode(obisCode2);
        registerMapping.save();

        // Asserts: expected CannotUpdateObisCodeWhenRegisterMappingIsInUseException
    }

    @Test(expected = CannotUpdateProductSpecWhenRegisterMappingIsInUseException.class)
    @Transactional
    public void testCannotUpdateProductSpecWhenUsedByRegisterSpec() {
        String registerMappingName = "testCannotUpdateProductSpecWhenUsedByRegisterSpec";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

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
        registerMapping.setProductSpec(this.productSpec2);
        registerMapping.save();

        // Asserts: expected CannotUpdateProductSpecWhenRegisterMappingIsInUseException
    }

    @Test(expected = CannotUpdateProductSpecWhenRegisterMappingIsInUseException.class)
    @Transactional
    public void testCannotUpdateProductSpecWhenUsedByChannelSpec() {
        String registerMappingName = "testCannotUpdateProductSpecWhenUsedByChannelSpec";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        this.setupPhenomenaInExistingTransaction();
        this.setupLoadProfileTypesInExistingTransaction();

        this.loadProfileType.addRegisterMapping(registerMapping);
        this.loadProfileType.save();

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(this.loadProfileType);
        deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
        configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder).setReadingMethod(ReadingMethod.BASIC_DATA).setMultiplierMode(MultiplierMode.NONE).setValueCalculationMethod(ValueCalculationMethod.AUTOMATIC);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        registerMapping.setProductSpec(productSpec2);
        registerMapping.save();

        // Asserts: expected CannotUpdateProductSpecWhenRegisterMappingIsInUseException
    }

    @Test
    @Transactional
    public void testDelete() {
        String registerMappingName = "testDelete";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Business method
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        long id = registerMapping.getId();
        registerMapping.delete();

        // Asserts
        RegisterMapping expectedNull = inMemoryPersistence.getDeviceConfigurationService().findRegisterMapping(id);
        assertThat(expectedNull).isNull();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCannotDeleteWhenUsedByRegisterSpecs() {
        String registerMappingName = "testCannotDeleteWhenUsedByRegisterSpecs";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

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
    public void testCannotDeleteWhenUsedByChannelSpecs() {
        String registerMappingName = "testCannotDeleteWhenUsedByChannelSpecs";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();


        this.setupPhenomenaInExistingTransaction();
        this.setupLoadProfileTypesInExistingTransaction();

        this.loadProfileType.addRegisterMapping(registerMapping);
        this.loadProfileType.save();

        // Use it in a DeviceType and DeviceConfiguration
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(this.loadProfileType);
        deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
        configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder).setReadingMethod(ReadingMethod.BASIC_DATA).setMultiplierMode(MultiplierMode.NONE).setValueCalculationMethod(ValueCalculationMethod.AUTOMATIC);
        configurationBuilder.add();
        deviceType.save();

        try {
            registerMapping.delete();
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_CHANNEL_SPEC);
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
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

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

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCannotDeleteWhenUsedByLoadProfileType() {
        String registerMappingName = "testCannotDeleteWhenUsedByLoadProfileType";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Use it in a LoadProfileType
        this.setupLoadProfileTypesInExistingTransaction(registerMapping);


        try {
            registerMapping.delete();
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE);
            throw e;
        }
    }

    private void setupProductSpecsInExistingTransaction() {
        this.setupReadingTypesInExistingTransaction();
        this.productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
        this.productSpec.save();
        this.productSpec2 = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType2);
        this.productSpec2.save();
    }

    private void setupReadingTypesInExistingTransaction() {
        String code = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.obisCode1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(readingType).getObisCode();
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
        this.loadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(RegisterMappingImplTest.class.getSimpleName(), ObisCode.fromString("1.0.99.1.0.255"), INTERVAL_15_MINUTES);
        this.loadProfileType.save();
    }

    private void setupLoadProfileTypesInExistingTransaction(RegisterMapping registerMapping) {
        this.setupLoadProfileTypesInExistingTransaction();
        this.loadProfileType.addRegisterMapping(registerMapping);
    }

    private void setupPhenomenaInExistingTransaction() {
        this.phenomenon = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(DeviceTypeImplTest.class.getSimpleName(), Unit.get("kWh"));
        this.phenomenon.save();
    }

}