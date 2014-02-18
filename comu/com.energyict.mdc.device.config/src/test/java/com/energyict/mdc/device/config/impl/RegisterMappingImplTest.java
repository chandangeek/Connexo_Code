package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionContext;
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
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ObisCodeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ProductSpecIsRequiredException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the peristence aspects of the {@link RegisterMappingImpl} component
 * as provided by the {@link DeviceConfigurationServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterMappingImplTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.MINUTES);
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    private InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();
    private LoadProfileType loadProfileType;
    private ReadingType readingType;
    private ReadingType readingType2;
    private ProductSpec productSpec;
    private ProductSpec productSpec2;
    private Phenomenon phenomenon;
    private ObisCode obisCode1;
    private ObisCode obisCode2;

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase("DeviceTypeImplTest.mdc.device.config");
        this.initializeMocks();
    }

    private void initializeMocks() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testCreateWithoutViolations () {
        String registerMappingName = "testCreateWithoutViolations";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Business method
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
            ctx.commit();
        }

        // Asserts
        assertThat(registerMapping).isNotNull();
        assertThat(registerMapping.getId()).isGreaterThan(0);
        assertThat(registerMapping.getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping.getDescription()).isNotEmpty();
        assertThat(registerMapping.getReadingType()).isEqualTo(this.readingType);
        assertThat(registerMapping.getObisCode()).isEqualTo(obisCode1);
    }

    @Test
    public void testFindAfterCreation () {
        String registerMappingName = "testFindAfterCreation";
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            RegisterMapping registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
            ctx.commit();
        }

        // Business method
        RegisterMapping registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().findRegisterMappingByObisCodeAndProductSpec(obisCode1, this.productSpec);

        // Asserts
        assertThat(registerMapping).isNotNull();
        assertThat(registerMapping.getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping.getDescription()).isNotEmpty();
        assertThat(registerMapping.getReadingType()).isEqualTo(this.readingType);
        assertThat(registerMapping.getObisCode()).isEqualTo(obisCode1);
    }

    @Test(expected = NameIsRequiredException.class)
    public void testCreateWithoutName () {
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Business method
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(null, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
            ctx.commit();
        }
        catch (NameIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_NAME_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = NameIsRequiredException.class)
    public void testCreateWithEmptyName () {
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Business method
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("", obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
            ctx.commit();
        }
        catch (NameIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_NAME_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = ObisCodeIsRequiredException.class)
    public void testCreateWithoutObisCode () {
        String registerMappingName = "testCreateWithoutObisCode";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Business method
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, null, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
            ctx.commit();
        }
        catch (ObisCodeIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = ProductSpecIsRequiredException.class)
    public void testCreateWithoutProductSpec () {
        String registerMappingName = "testCreateWithoutProductSpec";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, null);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
            ctx.commit();
        }

        // Asserts: expected ProductSpecIsRequiredException
    }

    @Test
    public void testUpdateObisCodeAndProductSpec() {
        String registerMappingName = "testUpdateObisCode";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            registerMapping.setObisCode(obisCode2);
            registerMapping.setProductSpec(productSpec2);
            registerMapping.save();
            ctx.commit();
        }

        // Asserts
        assertThat(registerMapping.getObisCode()).isEqualTo(obisCode2);
        assertThat(registerMapping.getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping.getDescription()).isNotEmpty();
        assertThat(registerMapping.getReadingType()).isEqualTo(this.readingType2);
    }

    @Test(expected = DuplicateObisCodeException.class)
    public void testUpdateObisCodeWithDuplicate () {
        String registerMappingName = "testUpdateObisCodeWithDuplicate";
        RegisterMapping updateCandidate;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            updateCandidate = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            updateCandidate.setDescription("For testing purposes only");
            updateCandidate.save();

            RegisterMapping other = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("other", obisCode2, this.productSpec);
            other.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            updateCandidate.setObisCode(obisCode2);
            updateCandidate.save();
            ctx.commit();
        }
        catch (DuplicateObisCodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_ALREADY_EXISTS);
            throw e;
        }
    }

    @Test(expected = CannotUpdateObisCodeWhenRegisterMappingIsInUseException.class)
    public void testCannotUpdateObisCodeWhenUsedByRegisterSpec () {
        String registerMappingName = "testCannotUpdateObisCodeWhenUsedByRegisterSpec";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Create the RegisterMapping
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();

            // Use it in a DeviceType and DeviceConfiguration
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            RegisterSpec.RegisterSpecBuilder registerSpecBuilder = configurationBuilder.newRegisterSpec(registerMapping);
            registerSpecBuilder.setNumberOfDigits(5);
            registerSpecBuilder.setNumberOfFractionDigits(2);
            registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
            configurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            registerMapping.setObisCode(obisCode2);
            registerMapping.save();
            ctx.commit();
        }

        // Asserts: expected CannotUpdateObisCodeWhenRegisterMappingIsInUseException
    }

    @Test(expected = CannotUpdateObisCodeWhenRegisterMappingIsInUseException.class)
    public void testCannotUpdateObisCodeWhenUsedByChannelSpec () {
        String registerMappingName = "testCannotUpdateObisCodeWhenUsedByChannelSpec";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Create the RegisterMapping
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();

            this.setupPhenomenaInExistingTransaction();
            this.setupLoadProfileTypesInExistingTransaction();

            // Use it in a DeviceType and DeviceConfiguration
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.addRegisterMapping(registerMapping);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
            configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder);
            configurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            registerMapping.setObisCode(obisCode2);
            registerMapping.save();
            ctx.commit();
        }

        // Asserts: expected CannotUpdateObisCodeWhenRegisterMappingIsInUseException
    }

    @Test
    public void testUpdateProductSpec () {
        String registerMappingName = "testUpdateProductSpec";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Create the RegisterMapping
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            registerMapping.setProductSpec(this.productSpec2);
            registerMapping.save();
            ctx.commit();
        }

        // Asserts
        assertThat(registerMapping.getObisCode()).isEqualTo(obisCode1);
        assertThat(registerMapping.getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping.getDescription()).isNotEmpty();
        assertThat(registerMapping.getReadingType()).isEqualTo(this.readingType2);
    }

    @Test(expected = CannotUpdateProductSpecWhenRegisterMappingIsInUseException.class)
    public void testCannotUpdateProductSpecWhenUsedByRegisterSpec () {
        String registerMappingName = "testCannotUpdateProductSpecWhenUsedByRegisterSpec";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Create the RegisterMapping
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();

            // Use it in a DeviceType and DeviceConfiguration
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            RegisterSpec.RegisterSpecBuilder registerSpecBuilder = configurationBuilder.newRegisterSpec(registerMapping);
            registerSpecBuilder.setNumberOfDigits(5);
            registerSpecBuilder.setNumberOfFractionDigits(2);
            registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
            configurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            registerMapping.setProductSpec(this.productSpec2);
            registerMapping.save();
            ctx.commit();
        }

        // Asserts: expected CannotUpdateProductSpecWhenRegisterMappingIsInUseException
    }

    @Test(expected = CannotUpdateProductSpecWhenRegisterMappingIsInUseException.class)
    public void testCannotUpdateProductSpecWhenUsedByChannelSpec () {
        String registerMappingName = "testCannotUpdateProductSpecWhenUsedByChannelSpec";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Create the RegisterMapping
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();

            this.setupPhenomenaInExistingTransaction();
            this.setupLoadProfileTypesInExistingTransaction();

            // Use it in a DeviceType and DeviceConfiguration
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.addRegisterMapping(registerMapping);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
            configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder);
            configurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            registerMapping.setProductSpec(productSpec2);
            registerMapping.save();
            ctx.commit();
        }

        // Asserts: expected CannotUpdateProductSpecWhenRegisterMappingIsInUseException
    }

    @Test
    public void testDelete() {
        String registerMappingName = "testDelete";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Business method
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();
            ctx.commit();
        }

        long id = registerMapping.getId();
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            registerMapping.delete();
            ctx.commit();
        }

        // Asserts
        RegisterMapping expectedNull = this.inMemoryPersistence.getDeviceConfigurationService().findRegisterMapping(id);
        assertThat(expectedNull).isNull();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testCannotDeleteWhenUsedByRegisterSpecs () {
        String registerMappingName = "testCannotDeleteWhenUsedByRegisterSpecs";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Create the RegisterMapping
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();

            // Use it in a DeviceType and DeviceConfiguration
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            RegisterSpec.RegisterSpecBuilder registerSpecBuilder = configurationBuilder.newRegisterSpec(registerMapping);
            registerSpecBuilder.setNumberOfDigits(5);
            registerSpecBuilder.setNumberOfFractionDigits(2);
            registerSpecBuilder.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
            configurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            registerMapping.delete();
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_REGISTER_SPEC);
            throw e;
        }
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testCannotDeleteWhenUsedByChannelSpecs () {
        String registerMappingName = "testCannotDeleteWhenUsedByChannelSpecs";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Create the RegisterMapping
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();

            this.setupPhenomenaInExistingTransaction();
            this.setupLoadProfileTypesInExistingTransaction();

            // Use it in a DeviceType and DeviceConfiguration
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.addRegisterMapping(registerMapping);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(this.loadProfileType);
            configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder);
            configurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            registerMapping.delete();
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_CHANNEL_SPEC);
            throw e;
        }
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testCannotDeleteWhenUsedByDeviceType () {
        String registerMappingName = "testCannotDeleteWhenUsedByDeviceType";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Create the RegisterMapping
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();

            // Use it in a DeviceType and DeviceConfiguration
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(registerMappingName, this.deviceProtocolPluggableClass);
            deviceType.addRegisterMapping(registerMapping);
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            registerMapping.delete();
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_DEVICE_TYPE);
            throw e;
        }
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testCannotDeleteWhenUsedByLoadProfileType() {
        String registerMappingName = "testCannotDeleteWhenUsedByLoadProfileType";
        RegisterMapping registerMapping;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupProductSpecsInExistingTransaction();

            // Create the RegisterMapping
            registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingName, obisCode1, this.productSpec);
            registerMapping.setDescription("For testing purposes only");
            registerMapping.save();

            // Use it in a LoadProfileType
            this.setupLoadProfileTypesInExistingTransaction(registerMapping);

            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            registerMapping.delete();
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE);
            throw e;
        }
    }

    private void setupProductSpecsInExistingTransaction () {
        this.setupReadingTypesInExistingTransaction();
        this.productSpec = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
        this.productSpec.save();
        this.productSpec2 = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType2);
        this.productSpec2.save();
    }

    private void setupReadingTypesInExistingTransaction() {
        String code = ReadingTypeCodeBuilder
                    .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType = this.inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.obisCode1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(readingType).getObisCode();
        String code2 = ReadingTypeCodeBuilder
                    .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.REVERSE)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType2 = this.inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        this.obisCode2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(readingType2).getObisCode();
    }

    private void setupLoadProfileTypesInExistingTransaction() {
        this.loadProfileType = this.inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(RegisterMappingImplTest.class.getSimpleName(), ObisCode.fromString("1.0.99.1.0.255"), INTERVAL_15_MINUTES);
        this.loadProfileType.save();
    }

    private void setupLoadProfileTypesInExistingTransaction(RegisterMapping registerMapping) {
        this.setupLoadProfileTypesInExistingTransaction();
        this.loadProfileType.addRegisterMapping(registerMapping);
    }

    private void setupPhenomenaInExistingTransaction () {
        this.phenomenon = this.inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(DeviceTypeImplTest.class.getSimpleName(), Unit.get("kWh"));
        this.phenomenon.save();
    }

}