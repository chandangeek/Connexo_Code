/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceUsageType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigurationIsActiveException;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.LogBookTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.RegisterTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.FlowDirection.REVERSE;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the persistence aspects of the {@link DeviceTypeImpl} component
 * as provided by the {@link DeviceConfigurationServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (17:44)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceTypeImplTest extends DeviceTypeProvidingPersistenceTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.TimeUnit.MINUTES);
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID_2 = 149;
    private final BigDecimal overflowValue = BigDecimal.valueOf(10000);

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private ServerDeviceConfiguration deviceConfig;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass2;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceProtocol deviceProtocol2;

    private ReadingType readingType1;
    private ReadingType readingType2;
    private LogBookType logBookType;
    private LogBookType logBookType2;
    private LoadProfileType loadProfileType;
    private LoadProfileType loadProfileType2;
    private RegisterType registerType1;
    private RegisterType registerType2;

    @Before
    public void initializeDatabaseAndMocks() {
        this.initMocks();
    }

    private void initMocks() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.deviceProtocolPluggableClass2.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID_2);
        when(this.deviceProtocolPluggableClass2.getDeviceProtocol()).thenReturn(this.deviceProtocol2);
    }

    @Test
    @Transactional
    public void testDeviceTypeCreation() {
        String deviceTypeName = "testDeviceTypeCreation";
        DeviceType deviceType;
        // Business method
        DeviceType.DeviceTypeBuilder deviceTypeBuilder = inMemoryPersistence.getDeviceConfigurationService()
                .newDeviceTypeBuilder(deviceTypeName, this.deviceProtocolPluggableClass, inMemoryPersistence.getDeviceLifeCycleConfigurationService()
                        .findDefaultDeviceLifeCycle()
                        .get());
        deviceTypeBuilder.setDescription("For testing purposes only");
        deviceType = deviceTypeBuilder.create();

        // Asserts
        assertThat(deviceType).isNotNull();
        assertThat(deviceType.getId()).isGreaterThan(0);
        assertThat(deviceType.getName()).isEqualTo(deviceTypeName);
        assertThat(deviceType.getLogBookTypes()).isEmpty();
        assertThat(deviceType.getLoadProfileTypes()).isEmpty();
        assertThat(deviceType.getRegisterTypes()).isEmpty();
        assertThat(deviceType.getDeviceProtocolPluggableClass().get()).isEqualTo(this.deviceProtocolPluggableClass);
        assertThat(deviceType.getDescription()).isNotEmpty();
        assertThat(deviceType.getDeviceUsageType()).isEqualTo(DeviceUsageType.NONE);
    }

    @Test
    @Transactional
    public void testFindDeviceTypeAfterCreation() {
        String deviceTypeName = "testFindDeviceTypeAfterCreation";
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");

        // Business method
        Optional<DeviceType> deviceType2 = inMemoryPersistence.getDeviceConfigurationService().findDeviceTypeByName(deviceTypeName);

        // Asserts
        assertThat(deviceType2.isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testCanActAsGateway() throws Exception {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        String deviceTypeName = "canActAsGateway";
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");

        assertThat(deviceType.canActAsGateway()).isTrue();
        assertThat(deviceType.isDirectlyAddressable()).isFalse();

    }

    @Test
    @Transactional
    public void testIsDirectlyAddressable() throws Exception {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION));
        String deviceTypeName = "directaddress";
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");

        assertThat(deviceType.isDirectlyAddressable()).isTrue();
        assertThat(deviceType.canActAsGateway()).isFalse();

    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_UNIQUE + "}")
    public void testDuplicateDeviceTypeCreation() {
        String deviceTypeName = "testDuplicateDeviceTypeCreation";
        // Setup first device type
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");

        // Business method
        DeviceType deviceType2 = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType2.setDescription("For testing purposes only");

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void testDeviceTypeCreationWithoutName() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(null, this.deviceProtocolPluggableClass);

        // Asserts: See ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void testDeviceTypeCreationWithEmptyName() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("", this.deviceProtocolPluggableClass);

        // Asserts: See the ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    public void testDeviceTypeCreationWithTooLongAName() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(longUnicodeString(81), this.deviceProtocolPluggableClass);

        // Asserts: See the ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "description")
    public void testDeviceTypeCreationWithTooLongADescription() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(longUnicodeString(80), this.deviceProtocolPluggableClass);
        deviceType.setDescription(longUnicodeString(4001));

        // Business method
        deviceType.update();

        // Asserts: See the ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void testDeviceTypeCreationWithoutProtocol() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("testDeviceTypeCreationWithoutProtocol", (DeviceProtocolPluggableClass) null);

        // Asserts: See the ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testCreateDeviceTypeWithLogBookType() {
        String deviceTypeName = "testCreateDeviceTypeWithLogBookType";
        DeviceType deviceType;
        this.setupLogBookTypesInExistingTransaction(deviceTypeName);

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        // Business method
        deviceType.addLogBookType(this.logBookType);

        // Asserts
        assertThat(deviceType.getLogBookTypes()).containsOnly(this.logBookType);
    }

    @Test
    @Transactional
    public void testCreateDeviceTypeWithMultipleLogBookTypes() {
        String deviceTypeName = "testCreateDeviceTypeWithMultipleLogBookTypes";
        DeviceType deviceType;
        this.setupLogBookTypesInExistingTransaction(deviceTypeName);

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        // Business method
        deviceType.addLogBookType(this.logBookType);
        deviceType.addLogBookType(this.logBookType2);

        // Asserts
        assertThat(deviceType.getLogBookTypes()).containsOnly(this.logBookType, this.logBookType2);
    }

    @Test(expected = LogBookTypeAlreadyInDeviceTypeException.class)
    @Transactional
    public void testAddLogBookTypeThatIsAlreadyAdded() {
        String deviceTypeName = "testAddLogBookTypeThatIsAlreadyAdded";
        DeviceType deviceType;
        this.setupLogBookTypesInExistingTransaction(deviceTypeName);

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLogBookType(this.logBookType);

        // Business method
        deviceType.addLogBookType(this.logBookType);

        // Asserts: expected LogBookTypeAlreadyInDeviceTypeException
    }

    @Test
    @Transactional
    public void testAddLogBookType() {
        String deviceTypeName = "testAddLogBookType";
        DeviceType deviceType;
        this.setupLogBookTypesInExistingTransaction(deviceTypeName);

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLogBookType(this.logBookType);

        // Business method
        deviceType.addLogBookType(this.logBookType2);

        assertThat(deviceType.getLogBookTypes()).containsOnly(this.logBookType, this.logBookType2);
    }

    @Test
    @Transactional
    public void testRemoveLogBookType() {
        String deviceTypeName = "testRemoveLogBookType";
        DeviceType deviceType;
        this.setupLogBookTypesInExistingTransaction(deviceTypeName);

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLogBookType(this.logBookType);

        // Business method
        deviceType.removeLogBookType(this.logBookType);

        // Asserts
        assertThat(deviceType.getLogBookTypes()).isEmpty();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testRemoveLogBookTypeThatIsStillInUse() {
        String deviceTypeName = "testRemoveLogBookTypeThatIsStillInUse";
        DeviceType deviceType;
        this.setupLogBookTypesInExistingTransaction(deviceTypeName);

        // Setup device type
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLogBookType(this.logBookType);

        // Setup DeviceConfiguration that uses the LogBookType
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Config for " + deviceTypeName);
        deviceConfigurationBuilder.newLogBookSpec(this.logBookType);
        deviceConfigurationBuilder.add();

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
    @Transactional
    public void testCreateDeviceTypeWithLoadProfileType() {
        String deviceTypeName = "testCreateDeviceTypeWithLoadProfileType";
        DeviceType deviceType;
        this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        // Business method
        deviceType.addLoadProfileType(this.loadProfileType);

        // Asserts
        assertThat(deviceType.getLoadProfileTypes()).containsOnly(this.loadProfileType);
    }

    @Test
    @Transactional
    public void testCreateDeviceTypeWithMultipleLoadProfileTypes() {
        String deviceTypeName = "testCreateDeviceTypeWithMultipleLoadProfileTypes";
        DeviceType deviceType;
        this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        // Business method
        deviceType.addLoadProfileType(this.loadProfileType);
        deviceType.addLoadProfileType(this.loadProfileType2);

        // Asserts
        assertThat(deviceType.getLoadProfileTypes()).containsOnly(this.loadProfileType, this.loadProfileType2);
    }

    @Test(expected = LoadProfileTypeAlreadyInDeviceTypeException.class)
    @Transactional
    public void testAddLoadProfileTypeThatIsAlreadyAdded() {
        String deviceTypeName = "testAddLoadProfileTypeThatIsAlreadyAdded";
        DeviceType deviceType;
        this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLoadProfileType(this.loadProfileType);

        // Business method
        deviceType.addLoadProfileType(this.loadProfileType);

        // Asserts: expected LoadProfileTypeAlreadyInDeviceTypeException
    }

    @Test
    @Transactional
    public void testAddLoadProfileType() {
        String deviceTypeName = "testAddLoadProfileType";
        DeviceType deviceType;
        this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLoadProfileType(this.loadProfileType);

        // Business method
        deviceType.addLoadProfileType(this.loadProfileType2);

        assertThat(deviceType.getLoadProfileTypes()).containsOnly(this.loadProfileType, this.loadProfileType2);
    }

    @Test
    @Transactional
    public void testRemoveLoadProfileType() {
        String deviceTypeName = "testRemoveLoadProfileType";
        DeviceType deviceType;
        this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLoadProfileType(this.loadProfileType);

        // Business method
        deviceType.removeLoadProfileType(this.loadProfileType);

        // Asserts
        assertThat(deviceType.getLoadProfileTypes()).isEmpty();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testRemoveLoadProfileTypeThatIsStillInUse() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String deviceTypeName = "testRemoveLoadProfileTypeThatIsStillInUse";
        DeviceType deviceType;
        this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);

        // Setup the device type
        deviceType = deviceConfigurationService.newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLoadProfileType(this.loadProfileType);

        // Add device configuration with a LoadProfileSpec that uses the LoadProfileType
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Conf 1 for " + deviceTypeName);
        deviceConfigurationBuilder.newLoadProfileSpec(this.loadProfileType);
        deviceConfigurationBuilder.add();

        try {
            // Business method
            deviceType.removeLoadProfileType(this.loadProfileType);
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_LOAD_PROFILE_SPECS);
            throw e;
        }
    }

    @Test
    @Transactional
    public void testCreateDeviceTypeWithRegisterType() {
        String deviceTypeName = "testCreateDeviceTypeWithRegisterType";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        // Business method
        deviceType.addRegisterType(this.registerType1);

        // Asserts
        assertThat(deviceType.getRegisterTypes()).containsOnly(this.registerType1);
    }

    @Test
    @Transactional
    public void testUpdateDeviceTypeWithConfigWithSameProtocolDoesNotDetectChange() {
        // JP-1845
        String deviceTypeName = "testUpdateDeviceTypeWithConfigWithSameProtocolDoesNotDetectChange";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);

        deviceType.newConfiguration("first").description("at least one").add();

        deviceType = this.reloadCreatedDeviceType(deviceType.getId());
        // Business method
        deviceType.setDeviceProtocolPluggableClass(deviceProtocolPluggableClass);
        deviceType.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS + "}")
    public void testUpdateDeviceTypeWithConfigWithOtherProtocolDoesDetectChange() {
        // JP-1845
        String deviceTypeName = "testUpdateDeviceTypeWithConfigWithOtherProtocolDoesDetectChange";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);

        deviceType.newConfiguration("first").description("at least one").add();

        deviceType = this.reloadCreatedDeviceType(deviceType.getId());
        // Business method
        deviceType.setDeviceProtocolPluggableClass(deviceProtocolPluggableClass2);
        deviceType.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", strict=false)
    public void testUpdateDeviceTypeWithConfigSetNullProtocol() {
        // JP-1845
        String deviceTypeName = "testCreateDeviceTypeWithRegisterType";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);

        deviceType.newConfiguration("first").description("at least one").add();

        deviceType = this.reloadCreatedDeviceType(deviceType.getId());

        // Business method
        deviceType.setDeviceProtocolPluggableClass((DeviceProtocolPluggableClass) null);
        deviceType.update();
    }

    @Test
    @Transactional
    public void testCreateDeviceTypeWithMultipleRegisterTypes() {
        String deviceTypeName = "testCreateDeviceTypeWithMultipleRegisterTypes";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        // Business method
        deviceType.addRegisterType(this.registerType1);
        deviceType.addRegisterType(this.registerType2);

        // Asserts
        assertThat(deviceType.getRegisterTypes()).containsOnly(this.registerType1, this.registerType2);
    }

    @Test(expected = RegisterTypeAlreadyInDeviceTypeException.class)
    @Transactional
    public void testAddRegisterTypeThatIsAlreadyAdded() {
        String deviceTypeName = "testAddRegisterTypeThatIsAlreadyAdded";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);

        // Business method
        deviceType.addRegisterType(this.registerType1);

        // Asserts: expected RegisterTypeAlreadyInDeviceTypeException
    }

    @Test
    @Transactional
    public void testAddRegisterType() {
        String deviceTypeName = "testAddRegisterType";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");

        // Business method
        deviceType.addRegisterType(this.registerType1);

        // Asserts
        assertThat(deviceType.getRegisterTypes()).containsOnly(this.registerType1);
    }

    @Test
    @Transactional
    public void testRemoveRegisterType() {
        String deviceTypeName = "testRemoveRegisterType";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);

        // Business method
        deviceType.removeRegisterType(this.registerType1);

        // Asserts
        assertThat(deviceType.getRegisterTypes()).isEmpty();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testRemoveRegisterTypeThatIsStillInUseByRegisterSpec() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String deviceTypeName = "testRemoveRegisterTypeThatIsStillInUseByRegisterSpec";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        // Setup the device type
        deviceType = deviceConfigurationService.newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);

        // Add DeviceConfiguration with a RegisterSpec that uses the RegisterType
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Conf 1 for " + deviceTypeName);
        NumericalRegisterSpec.Builder registerSpecBuilder = deviceConfigurationBuilder.newNumericalRegisterSpec(this.registerType1);
        registerSpecBuilder.overflowValue(overflowValue);
        registerSpecBuilder.numberOfFractionDigits(2);
        deviceConfigurationBuilder.add();

        try {
            // Business method
            deviceType.removeRegisterType(this.registerType1);
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_TYPE_STILL_USED_BY_REGISTER_SPEC);
            throw e;
        }
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS + "}")
    public void testProtocolChangeNotAllowedWhenConfigurationsExist() {
        String deviceTypeName = "testProtocolChangeNotAllowedWhenConfigurationsExist";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Active Configuration").add();
        deviceConfiguration.activate();

        // Business method
        deviceType.setDeviceProtocolPluggableClass(this.deviceProtocolPluggableClass2);
        deviceType.update();

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    public void isLogicalSlaveDelegatesToDeviceProtocolClass() throws SQLException {
        String deviceTypeName = "isLogicalSlaveDelegatesToDeviceProtocolClass";
        DeviceType deviceType;
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");

        // Business method
        deviceType.isLogicalSlave();

        // Asserts
        verify(this.deviceProtocolPluggableClass).getDeviceProtocol();
    }

    @Test
    @Transactional
    public void isLogicalSlaveWhenProtocolClassSaysSo() throws SQLException {
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(new ArrayList<>(EnumSet.of(DeviceProtocolCapabilities.PROTOCOL_SLAVE)));
        String deviceTypeName = "isLogicalSlaveWhenProtocolClassSaysSo";
        DeviceType deviceType;
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");

        // Business method
        boolean isLogicalSlave = deviceType.isLogicalSlave();

        // Asserts
        assertThat(isLogicalSlave).isTrue();
    }

    @Test
    @Transactional
    public void isLogicalSlaveWhenProtocolClassHasMultipleCapabilities() throws SQLException {
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(new ArrayList<>(EnumSet.allOf(DeviceProtocolCapabilities.class)));
        String deviceTypeName = "isLogicalSlaveWhenProtocolClassHasMultipleCapabilities";
        DeviceType deviceType;
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");

        // Business method
        boolean isLogicalSlave = deviceType.isLogicalSlave();

        // Asserts
        assertThat(isLogicalSlave).isFalse();
    }

    @Test
    @Transactional
    public void testDeviceTypeDeletionRemovesLogBookTypes() {
        String deviceTypeName = "testDeviceTypeDeletionRemovesLogBookTypes";
        DeviceType deviceType;
        this.setupLogBookTypesInExistingTransaction(deviceTypeName);

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLogBookType(this.logBookType);
        deviceType.addLogBookType(this.logBookType2);
        long deviceTypeId = deviceType.getId();

        // Business method
        deviceType.delete();

        // Asserts
        List<DeviceTypeLogBookTypeUsage> usages = inMemoryPersistence.getDeviceConfigurationService().getDataModel().mapper(DeviceTypeLogBookTypeUsage.class).find("DEVICETYPEID", deviceTypeId);
        assertThat(usages).as("Was not expecting to find any logbook type usages for device type {0} after deletion", deviceType).isEmpty();
    }

    @Test
    @Transactional
    public void testDeviceTypeDeletionRemovesLoadProfileTypes() {
        String deviceTypeName = "testDeviceTypeDeletionRemovesLoadProfileTypes";
        DeviceType deviceType;
        this.setupLoadProfileTypesInExistingTransaction(deviceTypeName);

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLoadProfileType(this.loadProfileType);
        deviceType.addLoadProfileType(this.loadProfileType2);
        long deviceTypeId = deviceType.getId();

        // Business method
        deviceType.delete();

        // Asserts
        List<DeviceTypeLoadProfileTypeUsage> usages = inMemoryPersistence.getDeviceConfigurationService().getDataModel().mapper(DeviceTypeLoadProfileTypeUsage.class).find("DEVICETYPEID", deviceTypeId);
        assertThat(usages).as("Was not expecting to find any load profile type usages for device type {0} after deletion", deviceType).isEmpty();

    }

    @Test
    @Transactional
    public void testDeviceTypeDeletionWithOnlyNonActiveDeviceConfigs() {
        String deviceTypeName = "test";
        DeviceType deviceType;

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.newConfiguration("first").description("this is it!").add();
        deviceType.newConfiguration("second").description("this is it!").add();

        long deviceTypeId = deviceType.getId();

        // Business method
        deviceType.delete();

        // Asserts
        Optional<DeviceType> reloaded = inMemoryPersistence.getDeviceConfigurationService().findDeviceType(deviceTypeId);
        assertThat(reloaded.isPresent()).isFalse();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCanNotDeleteDeviceTypeWithActiveDeviceConfig() {
        String deviceTypeName = "test";
        DeviceType deviceType;

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.newConfiguration("first").description("this is it!").add();
        DeviceConfiguration second = deviceType.newConfiguration("second").description("this is it!").add();
        second.activate();

        // Business method
        deviceType.delete();
    }

    @Test
    @Transactional
    public void deleteDeviceTypeWithInActiveDeviceConfigsTest() {
        String deviceTypeName = "test";
        DeviceType deviceType;

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        DeviceConfiguration first = deviceType.newConfiguration("first").description("this is it!").add();
        DeviceConfiguration second = deviceType.newConfiguration("second").description("this is it!").add();
        second.activate();
        second.createLoadProfileSpec(loadProfileType);
        second.save();
        second.deactivate();
        second.save();

        long firstId = first.getId();
        long secondId = second.getId();

        deviceType.delete();

        Optional<DeviceConfiguration> deviceConfiguration1 = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(firstId);
        assertThat(deviceConfiguration1.isPresent()).isFalse();
        Optional<DeviceConfiguration> deviceConfiguration2 = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(secondId);
        assertThat(deviceConfiguration2.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testDeviceTypeDeletionRemovesRegisterTypes() {
        String deviceTypeName = "testDeviceTypeDeletionRemovesRegisterTypes";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);
        deviceType.addRegisterType(this.registerType2);
        long deviceTypeId = deviceType.getId();

        // Business method
        deviceType.delete();

        // Asserts
        List<DeviceTypeRegisterTypeUsage> usages = inMemoryPersistence.getDeviceConfigurationService().getDataModel().mapper(DeviceTypeRegisterTypeUsage.class).find("DEVICETYPEID", deviceTypeId);
        assertThat(usages).as("Was not expecting to find any register mapping usages for device type {0} after deletion", deviceType).isEmpty();
    }

    @Test
    @Transactional
    public void testAddDeviceConfiguration() throws Exception {
        deviceType.newConfiguration("first").description("this is it!").add();

        DeviceType refreshed = this.reloadCreatedDeviceType(deviceType.getId());
        assertThat(refreshed.getConfigurations()).hasSize(1);
        assertThat(refreshed.getConfigurations().get(0).getName()).isEqualTo("first");
    }

    private DeviceType reloadCreatedDeviceType(long id) {
        return inMemoryPersistence.getDeviceConfigurationService().findDeviceType(id).orElseThrow(() -> new RuntimeException("DeviceType that was just created was not found"));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.FIELD_IS_REQUIRED +"}", property = "name")
    public void testCanNotAddDeviceConfigurationWithoutName() throws Exception {
        deviceType.newConfiguration(null).description("this is it!").add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.FIELD_IS_REQUIRED +"}", property = "name")
    public void testCanNotAddDeviceConfigurationWithEmptyName() throws Exception {
        deviceType.newConfiguration("").description("this is it!").add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.FIELD_TOO_LONG +"}", property = "name")
    public void testCanNotAddDeviceConfigurationWithLongName() throws Exception {
        deviceType.newConfiguration("01234567890123456789012345678901234567890123456789012345678901234567890123456789-").description("desc").add(); // 81 chars
    }

    @Test
    @Transactional
    public void testCanNotAddDeviceConfigurationWithMaxUnicodeCharsDescription() throws Exception {
        String string = longUnicodeString(4000);
        deviceType.newConfiguration("name").description(string).add();
    }

    private String longUnicodeString(int size) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<size; i++) {
            stringBuilder.append("\u8fd1");
        }
        return stringBuilder.toString();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.FIELD_TOO_LONG +"}", property = "description")
    public void testCanNotAddDeviceConfigurationWithTooManyUnicodeCharsDescription() throws Exception {
        deviceType.newConfiguration("name").description(longUnicodeString(4001)).add();
    }

    @Test
    @Transactional
    public void testRemoveDeviceConfigFromDeviceType() throws Exception {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("first").description("this is it!").add();

        deviceType.removeConfiguration(deviceConfiguration);
        Optional<DeviceConfiguration> reloaded = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId());
        assertThat(reloaded.isPresent()).isFalse();
    }

    @Test(expected = DeviceConfigurationIsActiveException.class)
    @Transactional
    public void testCanNotRemoveDeviceConfigIfInUse() throws Exception {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("first").description("this is it!").add();
        deviceConfiguration.activate();

        deviceType.removeConfiguration(deviceConfiguration);
    }

    @Test
    @Transactional
    public void testCanRemoveDeviceConfigurationWithLinkedValidationAndEstimationRuleSets() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("first").description("this is it!").add();

        ValidationRuleSet validationRuleSet = inMemoryPersistence.getValidationService().createValidationRuleSet("ValidationRuleSet", QualityCodeSystem.MDC);
        validationRuleSet.save();
        deviceConfiguration.addValidationRuleSet(validationRuleSet);

        EstimationRuleSet estimationRuleSet = inMemoryPersistence.getEstimationService().createEstimationRuleSet("EstimationRuleSet", QualityCodeSystem.MDC);
        estimationRuleSet.save();
        deviceConfiguration.addEstimationRuleSet(estimationRuleSet);

        deviceType.removeConfiguration(deviceConfiguration);
    }

    @Test
    @Transactional
    public void deleteDeviceTypeWithMessageFiles() throws IOException {
        this.deviceType.enableFileManagement();
        FileSystem jimfs = Jimfs.newFileSystem(Configuration.unix());
        Path path = jimfs.getPath("/temp.txt");
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(path))) {
            writer.write("deleteDeviceTypeWithMessageFiles");
        }
        this.deviceType.addDeviceMessageFile(path);
        long deviceTypeId = this.deviceType.getId();

        // Business method
        this.deviceType.delete();

        // Asserts
        AssertionsForClassTypes.assertThat(inMemoryPersistence.getDeviceConfigurationService().findDeviceType(deviceTypeId)).isEmpty();
    }

    @Test
    @Transactional
    public void addKeyAccessorType() throws Exception {
        inMemoryPersistence.getPkiService().newSymmetricKeyType("AES128", "AES", 128).add();

        Optional<KeyType> aes128 = inMemoryPersistence.getPkiService().getKeyType("AES128");
        assertThat(deviceType.getKeyAccessorTypes()).isEmpty();
        this.deviceType.addKeyAccessorType("GUAK", aes128.get()).keyEncryptionMethod("SSM").description("general use AK").duration(TimeDuration.days(365)).add();
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .findDeviceType(this.deviceType.getId()).get();
        assertThat(deviceType.getKeyAccessorTypes()).hasSize(1);
        assertThat(deviceType.getKeyAccessorTypes().get(0).getName()).isEqualTo("GUAK");
        assertThat(deviceType.getKeyAccessorTypes().get(0).getKeyEncryptionMethod()).isEqualTo("SSM");
        assertThat(deviceType.getKeyAccessorTypes().get(0).getKeyType().getName()).isEqualTo("AES128");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.FIELD_IS_REQUIRED+"}", property = "keyEncryptionMethod")
    public void addKeyAccessorTypeWithoutKeyEncryptionMethod() throws Exception {
        inMemoryPersistence.getPkiService().newSymmetricKeyType("AES256", "AES", 256).add();

        Optional<KeyType> aes128 = inMemoryPersistence.getPkiService().getKeyType("AES256");
        assertThat(deviceType.getKeyAccessorTypes()).isEmpty();
        this.deviceType.addKeyAccessorType("GUAK", aes128.get()).description("general use AK").duration(TimeDuration.days(365)).add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.FIELD_IS_REQUIRED+"}", property = "duration")
    public void addKeyAccessorTypeWithoutDuration() throws Exception {
        inMemoryPersistence.getPkiService().newSymmetricKeyType("AES256", "AES", 256).add();

        Optional<KeyType> aes128 = inMemoryPersistence.getPkiService().getKeyType("AES256");
        assertThat(deviceType.getKeyAccessorTypes()).isEmpty();
        this.deviceType.addKeyAccessorType("GUAK", aes128.get()).description("general use AK").keyEncryptionMethod("DataVault").add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.FIELD_IS_REQUIRED+"}", property = "trustStore")
    public void addCertificateAccessorTypeMissingTrustStore() throws Exception {
        inMemoryPersistence.getPkiService().newCertificateType("Friends").add();

        Optional<KeyType> certs = inMemoryPersistence.getPkiService().getKeyType("Friends");
        assertThat(deviceType.getKeyAccessorTypes()).isEmpty();
        this.deviceType.addKeyAccessorType("TLS", certs.get()).description("just certificates").add();
    }

    @Test
    @Transactional
    public void addCertificateAccessorType() throws Exception {
        inMemoryPersistence.getPkiService().newCertificateType("Friends").add();
        TrustStore main = inMemoryPersistence.getPkiService().newTrustStore("MAIN").add();

        Optional<KeyType> certs = inMemoryPersistence.getPkiService().getKeyType("Friends");
        assertThat(deviceType.getKeyAccessorTypes()).isEmpty();
        this.deviceType.addKeyAccessorType("TLS", certs.get())
                .description("just certificates")
                .trustStore(main)
                .add();
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .findDeviceType(this.deviceType.getId()).get();
        assertThat(deviceType.getKeyAccessorTypes()).hasSize(1);
        assertThat(deviceType.getKeyAccessorTypes().get(0).getName()).isEqualTo("TLS");
        assertThat(deviceType.getKeyAccessorTypes().get(0).getKeyEncryptionMethod()).isNull();
        assertThat(deviceType.getKeyAccessorTypes().get(0).getKeyType().getName()).isEqualTo("Friends");
        assertThat(deviceType.getKeyAccessorTypes().get(0).getTrustStore()).isPresent();
        assertThat(deviceType.getKeyAccessorTypes().get(0).getTrustStore().get().getName()).isEqualTo("MAIN");
    }

    @Test
    @Transactional
    public void testRemoveKeyAccessorType() throws Exception {
        inMemoryPersistence.getPkiService().newSymmetricKeyType("AES256", "AES", 256).add();

        Optional<KeyType> aes128 = inMemoryPersistence.getPkiService().getKeyType("AES256");
        KeyAccessorType keyAccessorType = this.deviceType.addKeyAccessorType("GUAK", aes128.get())
                .description("general use AK")
                .duration(TimeDuration.days(365))
                .keyEncryptionMethod("DataVault")
                .add();

        // Test method
        this.deviceType.removeKeyAccessorType(keyAccessorType);

        assertThat(deviceType.getKeyAccessorTypes()).isEmpty();
    }

    private void setupLogBookTypesInExistingTransaction(String logBookTypeBaseName) {
        this.logBookType = inMemoryPersistence.getMasterDataService().newLogBookType(logBookTypeBaseName + "-1", ObisCode.fromString("0.0.99.98.0.255"));
        this.logBookType.save();
        this.logBookType2 = inMemoryPersistence.getMasterDataService().newLogBookType(logBookTypeBaseName + "-2", ObisCode.fromString("1.0.99.97.0.255"));
        this.logBookType2.save();
    }

    private void setupLoadProfileTypesInExistingTransaction(String loadProfileTypeBaseName) {
        this.setupRegisterTypesInExistingTransaction();
        this.loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(loadProfileTypeBaseName + "-1", ObisCode.fromString("1.0.99.1.0.255"), INTERVAL_15_MINUTES, Arrays.asList(registerType1));
        this.loadProfileType.save();
        this.loadProfileType2 = inMemoryPersistence.getMasterDataService().newLoadProfileType(loadProfileTypeBaseName + "-2", ObisCode.fromString("1.0.99.2.0.255"), INTERVAL_15_MINUTES, Arrays.asList(registerType2));
        this.loadProfileType2.save();
    }

    private void setupRegisterTypesInExistingTransaction() {
        this.setupProductSpecsInExistingTransaction();
        Unit unit = Unit.get("kWh");
        this.registerType1 =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1).get();
        this.registerType1.save();
        this.registerType2 =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType2).get();
        this.registerType2.save();
    }

    private void setupProductSpecsInExistingTransaction() {
        this.setupReadingTypeInExistingTransaction();
    }

    private void setupReadingTypeInExistingTransaction() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED)
                .flow(FORWARD)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .accumulate(Accumulation.BULKQUANTITY)
                .code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        String code2 = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED)
                .flow(REVERSE)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .accumulate(Accumulation.BULKQUANTITY)
                .code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
    }

}
