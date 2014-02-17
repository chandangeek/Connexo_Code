package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceUsageType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotChangeDeviceProtocolWithActiveConfigurationsException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.DeviceProtocolIsRequiredException;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.LogBookTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingAlreadyInDeviceTypeException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the peristence aspects of the {@link DeviceTypeImpl} component
 * as provided by the {@link DeviceConfigurationServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (17:44)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceTypeImplTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.MINUTES);
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID_2 = 149;

    private InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private ServerDeviceConfiguration deviceConfig;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass2;
    @Mock
    private DeviceProtocol deviceProtocol;

    private ReadingType readingType;
    private Phenomenon phenomenon;
    private LogBookType logBookType;
    private LogBookType logBookType2;
    private LoadProfileType loadProfileType;
    private LoadProfileType loadProfileType2;
    private RegisterMapping registerMapping;
    private RegisterMapping registerMapping2;
    private ProductSpec productSpec;

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase("DeviceTypeImplTest.mdc.device.config");
        this.initializeMocks();
    }

    private void initializeMocks() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.deviceProtocolPluggableClass2.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID_2);

        when(deviceCommunicationConfiguration.getDeviceConfiguration()).thenReturn(deviceConfig);

        when(deviceConfig.getCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testDeviceTypeCreation() {
        String deviceTypeName = "testDeviceTypeCreation";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.save();
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType).isNotNull();
        assertThat(deviceType.getId()).isGreaterThan(0);
        assertThat(deviceType.getName()).isEqualTo(deviceTypeName);
        assertThat(deviceType.getLogBookTypes()).isEmpty();
        assertThat(deviceType.getLoadProfileTypes()).isEmpty();
        assertThat(deviceType.getRegisterMappings()).isEmpty();
        assertThat(deviceType.getCommunicationFunctions()).isEmpty();
        assertThat(deviceType.getDeviceProtocolPluggableClass()).isEqualTo(this.deviceProtocolPluggableClass);
        assertThat(deviceType.getDescription()).isNotEmpty();
        assertThat(deviceType.getDeviceUsageType()).isEqualTo(DeviceUsageType.NONE);
    }

    @Test
    public void testFindDeviceTypeAfterCreation() {
        String deviceTypeName = "testFindDeviceTypeAfterCreation";
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.save();
            ctx.commit();
        }

        // Business method
        DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().findDeviceTypeByName(deviceTypeName);

        // Asserts
        assertThat(deviceType).isNotNull();
    }

    @Test(expected = NameIsRequiredException.class)
    public void testDeviceTypeCreationWithoutName() {
        // Business method
        this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(null, this.deviceProtocolPluggableClass);

        // Asserts: Should be getting a NameIsRequiredException
    }

    @Test(expected = NameIsRequiredException.class)
    public void testDeviceTypeCreationWithEmptyName() {
        // Business method
        this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType("", this.deviceProtocolPluggableClass);

        // Asserts: Should be getting a NameIsRequiredException
    }

    @Test(expected = DeviceProtocolIsRequiredException.class)
    public void testDeviceTypeCreationWithoutProtocol() {
        // Business method
        this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType("testDeviceTypeCreationWithoutProtocol", (DeviceProtocolPluggableClass) null);

        // Asserts: Should be getting a DeviceProtocolIsRequiredException
    }

    @Test(expected = DeviceProtocolIsRequiredException.class)
    public void testDeviceTypeCreationWithNonExistingProtocol() {
        // Business method
        this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType("testDeviceTypeCreationWithNonExistingProtocol", "testDeviceTypeCreationWithNonExistingProtocol");

        // Asserts: Should be getting a DeviceProtocolIsRequiredException
    }

    @Test
    public void testCreateDeviceTypeWithLogBookType () {
        String deviceTypeName = "testCreateDeviceTypeWithLogBookType";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLogBookTypesInExistingTransaction(deviceTypeName);

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            // Business method
            deviceType.addLogBookType(this.logBookType);
            deviceType.save();
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType.getLogBookTypes()).containsOnly(this.logBookType);
    }

    @Test
    public void testCreateDeviceTypeWithMultipleLogBookTypes () {
        String deviceTypeName = "testCreateDeviceTypeWithMultipleLogBookTypes";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLogBookTypesInExistingTransaction(deviceTypeName);

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            // Business method
            deviceType.addLogBookType(this.logBookType);
            deviceType.addLogBookType(this.logBookType2);
            deviceType.save();
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType.getLogBookTypes()).containsOnly(this.logBookType, this.logBookType2);
    }

    @Test(expected = LogBookTypeAlreadyInDeviceTypeException.class)
    public void testAddLogBookTypeThatIsAlreadyAdded () {
        String deviceTypeName = "testAddLogBookTypeThatIsAlreadyAdded";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLogBookTypesInExistingTransaction(deviceTypeName);

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLogBookType(this.logBookType);
            deviceType.save();
            ctx.commit();
        }

        // Business method
        deviceType.addLogBookType(this.logBookType);

        // Asserts: expected LogBookTypeAlreadyInDeviceTypeException
    }

    @Test
    public void testAddLogBookType () {
        String deviceTypeName = "testAddLogBookType";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLogBookTypesInExistingTransaction(deviceTypeName);

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLogBookType(this.logBookType);
            deviceType.save();
            ctx.commit();
        }

        // Business method
        deviceType.addLogBookType(this.logBookType2);
        deviceType.save();

        assertThat(deviceType.getLogBookTypes()).containsOnly(this.logBookType, this.logBookType2);
    }

    @Test
    public void testRemoveLogBookType () {
        String deviceTypeName = "testRemoveLogBookType";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLogBookTypesInExistingTransaction(deviceTypeName);

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLogBookType(this.logBookType);
            deviceType.save();
            ctx.commit();
        }

        // Business method
        deviceType.removeLogBookType(this.logBookType);

        // Asserts
        assertThat(deviceType.getLogBookTypes()).isEmpty();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testRemoveLogBookTypeThatIsStillInUse() {
        String deviceTypeName = "testRemoveLogBookTypeThatIsStillInUse";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLogBookTypesInExistingTransaction(deviceTypeName);

            // Setup device type
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLogBookType(this.logBookType);

            // Setup DeviceConfiguration that uses the LogBookType
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Config for " + deviceTypeName);
            deviceConfigurationBuilder.newLogBookSpec(this.logBookType);
            deviceConfigurationBuilder.add();

            deviceType.save();
            ctx.commit();
        }

        try {
            // Business method
            deviceType.removeLogBookType(this.logBookType);
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS);
            throw e;
        }
    }

    @Test
    public void testCreateDeviceTypeWithLoadProfileType () {
        String deviceTypeName = "testCreateDeviceTypeWithLoadProfileType";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            // Business method
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.save();
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType.getLoadProfileTypes()).containsOnly(this.loadProfileType);
    }

    @Test
    public void testCreateDeviceTypeWithMultipleLoadProfileTypes () {
        String deviceTypeName = "testCreateDeviceTypeWithMultipleLoadProfileTypes";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            // Business method
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.addLoadProfileType(this.loadProfileType2);
            deviceType.save();
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType.getLoadProfileTypes()).containsOnly(this.loadProfileType, this.loadProfileType2);
    }

    @Test(expected = LoadProfileTypeAlreadyInDeviceTypeException.class)
    public void testAddLoadProfileTypeThatIsAlreadyAdded () {
        String deviceTypeName = "testAddLoadProfileTypeThatIsAlreadyAdded";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.save();
            ctx.commit();
        }

        // Business method
        deviceType.addLoadProfileType(this.loadProfileType);

        // Asserts: expected LoadProfileTypeAlreadyInDeviceTypeException
    }

    @Test
    public void testAddLoadProfileType () {
        String deviceTypeName = "testAddLoadProfileType";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.addLoadProfileType(this.loadProfileType2);
            deviceType.save();
            ctx.commit();
        }

        assertThat(deviceType.getLoadProfileTypes()).containsOnly(this.loadProfileType, this.loadProfileType2);
    }

    @Test
    public void testRemoveLoadProfileType () {
        String deviceTypeName = "testRemoveLoadProfileType";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.removeLoadProfileType(this.loadProfileType);
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType.getLoadProfileTypes()).isEmpty();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testRemoveLoadProfileTypeThatIsStillInUse () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String deviceTypeName = "testRemoveLoadProfileTypeThatIsStillInUse";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);

            // Setup the device type
            deviceType = deviceConfigurationService.newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.save();

            // Add device configuration with a LoadProfileSpec that uses the LoadProfileType
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Conf 1 for " + deviceTypeName);
            deviceConfigurationBuilder.newLoadProfileSpec(this.loadProfileType);
            deviceConfigurationBuilder.add();
            ctx.commit();
        }

        try {
            // Business method
            deviceType.removeLoadProfileType(this.loadProfileType);
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed().getNumber()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_LOAD_PROFILE_SPECS);
            throw e;
        }
    }

    @Test
    public void testCreateDeviceTypeWithRegisterMapping () {
        String deviceTypeName = "testCreateDeviceTypeWithRegisterMapping";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupRegisterMappingTypesInExistingTransaction();

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            // Business method
            deviceType.addRegisterMapping(this.registerMapping);
            deviceType.save();
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType.getRegisterMappings()).containsOnly(this.registerMapping);
    }

    @Test
    public void testCreateDeviceTypeWithMultipleRegisterMappings () {
        String deviceTypeName = "testCreateDeviceTypeWithMultipleRegisterMappings";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupRegisterMappingTypesInExistingTransaction();

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            // Business method
            deviceType.addRegisterMapping(this.registerMapping);
            deviceType.addRegisterMapping(this.registerMapping2);
            deviceType.save();
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType.getRegisterMappings()).containsOnly(this.registerMapping, this.registerMapping2);
    }

    @Test(expected = RegisterMappingAlreadyInDeviceTypeException.class)
    public void testAddRegisterMappingThatIsAlreadyAdded () {
        String deviceTypeName = "testAddRegisterMappingThatIsAlreadyAdded";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupRegisterMappingTypesInExistingTransaction();

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addRegisterMapping(this.registerMapping);
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.addRegisterMapping(this.registerMapping);
            ctx.commit();
        }

        // Asserts: expected RegisterMappingAlreadyInDeviceTypeException
    }

    @Test
    public void testAddRegisterMapping () {
        String deviceTypeName = "testAddRegisterMapping";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupRegisterMappingTypesInExistingTransaction();

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addRegisterMapping(this.registerMapping);
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.addRegisterMapping(this.registerMapping);
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType.getRegisterMappings()).containsOnly(this.registerMapping, this.registerMapping2);
    }

    @Test
    public void testRemoveRegisterMapping () {
        String deviceTypeName = "testRemoveRegisterMapping";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupRegisterMappingTypesInExistingTransaction();

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addRegisterMapping(this.registerMapping);
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.removeRegisterMapping(this.registerMapping);
            ctx.commit();
        }

        // Asserts
        assertThat(deviceType.getRegisterMappings()).isEmpty();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testRemoveRegisterMappingThatIsStillInUseByRegisterSpec () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String deviceTypeName = "testRemoveRegisterMappingThatIsStillInUseByRegisterSpec";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupRegisterMappingTypesInExistingTransaction();

            // Setup the device type
            deviceType = deviceConfigurationService.newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addRegisterMapping(this.registerMapping);
            deviceType.save();

            // Add DeviceConfiguration with a RegisterSpec that uses the RegisterMapping
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Conf 1 for " + deviceTypeName);
            deviceConfigurationBuilder.newRegisterSpec(this.registerMapping);
            deviceConfigurationBuilder.add();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.removeRegisterMapping(this.registerMapping);
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_REGISTER_SPEC);
            throw e;
        }
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testRemoveRegisterMappingThatIsStillInUseByChannelSpec () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String deviceTypeName = "testRemoveRegisterMappingThatIsStillInUseByChannelSpec";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupPhenomenaInExistingTransaction();
            this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
            this.setupRegisterMappingTypesInExistingTransaction();

            // Setup the device type
            deviceType = deviceConfigurationService.newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addRegisterMapping(this.registerMapping);
            deviceType.save();

            // Add DeviceConfiguration with a ChannelSpec that uses the ChannelMapping
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Conf 1 for " + deviceTypeName);
            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(this.loadProfileType);
            deviceConfigurationBuilder.newChannelSpec(this.registerMapping, this.phenomenon, loadProfileSpecBuilder);
            deviceConfigurationBuilder.add();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.removeRegisterMapping(this.registerMapping);
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_CHANNEL_SPEC);
            throw e;
        }
    }

    @Test(expected = CannotChangeDeviceProtocolWithActiveConfigurationsException.class)
    public void testProtocolChangeNotAllowedWhenConfigurationsExist() {
        String deviceTypeName = "testProtocolChangeNotAllowedWhenConfigurationsExist";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupRegisterMappingTypesInExistingTransaction();

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addRegisterMapping(this.registerMapping);
            deviceType.save();
            ctx.commit();
        }

        // Business method
        deviceType.setDeviceProtocolPluggableClass(this.deviceProtocolPluggableClass2);

        // Asserts: expected CannotChangeDeviceProtocolWithActiveConfigurationsException
    }

    @Test
    public void testDeviceTypeDeletionRemovesLogBookTypes() throws SQLException {
        String deviceTypeName = "testDeviceTypeDeletionRemovesLogBookTypes";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLogBookTypesInExistingTransaction(deviceTypeName);

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLogBookType(this.logBookType);
            deviceType.addLogBookType(this.logBookType2);
            deviceType.save();
            ctx.commit();
        }
        long deviceTypeId = deviceType.getId();

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.delete();
            ctx.commit();
        }

        // Asserts
        SqlBuilder builder = new SqlBuilder("select count(*) from eislogbooktypeforrtutype where RTUTYPEID = ?");
        builder.bindLong(deviceTypeId);
        try (PreparedStatement statement = builder.getStatement(Environment.DEFAULT.get().getConnection())) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                int logBookTypeCounter = resultSet.getInt(1);
                assertThat(logBookTypeCounter).isZero();
            }
        }
    }

    @Test
    public void testDeviceTypeDeletionRemovesLoadProfileTypes() throws SQLException {
        String deviceTypeName = "testDeviceTypeDeletionRemovesLoadProfileTypes";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLoadProfileType(this.loadProfileType);
            deviceType.addLoadProfileType(this.loadProfileType2);
            deviceType.save();
            ctx.commit();
        }
        long deviceTypeId = deviceType.getId();

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.delete();
            ctx.commit();
        }

        // Asserts
        SqlBuilder builder = new SqlBuilder("select count(*) from eisloadprofiletypeforrtutype where RTUTYPEID = ?");
        builder.bindLong(deviceTypeId);
        try (PreparedStatement statement = builder.getStatement(Environment.DEFAULT.get().getConnection())) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                int loadProfileTypeCounter = resultSet.getInt(1);
                assertThat(loadProfileTypeCounter).isZero();
            }
        }
    }

    @Test
    public void testDeviceTypeDeletionRemovesRegisterMappings() throws SQLException {
        String deviceTypeName = "testDeviceTypeDeletionRemovesRegisterMappings";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupRegisterMappingTypesInExistingTransaction();

            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addRegisterMapping(this.registerMapping);
            deviceType.addRegisterMapping(this.registerMapping2);
            deviceType.save();
            ctx.commit();
        }
        long deviceTypeId = deviceType.getId();

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            deviceType.delete();
            ctx.commit();
        }

        // Asserts
        SqlBuilder builder = new SqlBuilder("select count(*) from eisregistermappingforrtutype where RTUTYPEID = ?");
        builder.bindLong(deviceTypeId);
        try (PreparedStatement statement = builder.getStatement(Environment.DEFAULT.get().getConnection())) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                int registerMappingCounter = resultSet.getInt(1);
                assertThat(registerMappingCounter).isZero();
            }
        }
    }

    @Test
    public void isLogicalSlaveDelegatesToDeviceProtocolClass () throws SQLException, BusinessException {
        String deviceTypeName = "isLogicalSlaveDelegatesToDeviceProtocolClass";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.save();
            ctx.commit();
        }

        // Business method
        deviceType.isLogicalSlave();

        // Asserts
        verify(this.deviceProtocolPluggableClass).getDeviceProtocol();
    }

    @Test
    public void isLogicalSlaveWhenProtocolClassSaysSo () throws SQLException, BusinessException {
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(new ArrayList<>(EnumSet.of(DeviceProtocolCapabilities.PROTOCOL_SLAVE)));
        String deviceTypeName = "isLogicalSlaveWhenProtocolClassSaysSo";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.save();
            ctx.commit();
        }

        // Business method
        boolean isLogicalSlave = deviceType.isLogicalSlave();

        // Asserts
        assertThat(isLogicalSlave).isTrue();
    }

    @Test
    public void isLogicalSlaveWhenProtocolClassHasMultipleCapabilities () throws SQLException, BusinessException {
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(new ArrayList<>(EnumSet.allOf(DeviceProtocolCapabilities.class)));
        String deviceTypeName = "isLogicalSlaveWhenProtocolClassHasMultipleCapabilities";
        DeviceType deviceType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.save();
            ctx.commit();
        }

        // Business method
        boolean isLogicalSlave = deviceType.isLogicalSlave();

        // Asserts
        assertThat(isLogicalSlave).isFalse();
    }

    private void setupPhenomenaInExistingTransaction () {
        this.phenomenon = this.inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(DeviceTypeImplTest.class.getSimpleName(), Unit.get("kWh"));
        this.phenomenon.save();
    }

    private void setupLogBookTypesInExistingTransaction(String logBookTypeBaseName) {
        this.logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeBaseName + "-1", ObisCode.fromString("0.0.99.98.0.255"));
        this.logBookType.save();
        this.logBookType2 = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeBaseName + "-2", ObisCode.fromString("1.0.99.97.0.255"));
        this.logBookType2.save();
    }

    private void setupLoadProfileTypesInExistingTransaction(String loadProfileTypeBaseName) {
        this.loadProfileType = this.inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(loadProfileTypeBaseName + "-1", ObisCode.fromString("1.0.99.1.0.255"), INTERVAL_15_MINUTES);
        this.loadProfileType.save();
        this.loadProfileType2 = this.inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(loadProfileTypeBaseName + "-2", ObisCode.fromString("1.0.99.2.0.255"), INTERVAL_15_MINUTES);
        this.loadProfileType2.save();
    }

    private void setupRegisterMappingTypesInExistingTransaction () {
        this.setupProductSpecsInExistingTransaction();
        String registerMappingTypeBaseName = DeviceTypeImplTest.class.getSimpleName();
        this.registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingTypeBaseName + "-1", ObisCode.fromString("1.0.99.1.0.255"), this.productSpec);
        this.registerMapping.save();
        this.registerMapping2 = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(registerMappingTypeBaseName + "-2", ObisCode.fromString("1.0.99.2.0.255"), this.productSpec);
        this.registerMapping2.save();
    }

    private void setupProductSpecsInExistingTransaction () {
        this.setupReadingTypeInExistingTransaction();
        this.productSpec = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
        this.productSpec.save();
    }

    private void setupReadingTypeInExistingTransaction () {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType = this.inMemoryPersistence.getMeteringService().getReadingType(code).get();
    }

}