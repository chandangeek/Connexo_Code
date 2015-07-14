package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.common.BusinessException;
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
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.RegisterTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

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
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.save();

        // Asserts
        assertThat(deviceType).isNotNull();
        assertThat(deviceType.getId()).isGreaterThan(0);
        assertThat(deviceType.getName()).isEqualTo(deviceTypeName);
        assertThat(deviceType.getLogBookTypes()).isEmpty();
        assertThat(deviceType.getLoadProfileTypes()).isEmpty();
        assertThat(deviceType.getRegisterTypes()).isEmpty();
        assertThat(deviceType.getDeviceProtocolPluggableClass()).isEqualTo(this.deviceProtocolPluggableClass);
        assertThat(deviceType.getDescription()).isNotEmpty();
        assertThat(deviceType.getDeviceUsageType()).isEqualTo(DeviceUsageType.NONE);
    }

    @Test
    @Transactional
    public void testFindDeviceTypeAfterCreation() {
        String deviceTypeName = "testFindDeviceTypeAfterCreation";
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

        // Business method
        DeviceType deviceType2 = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType2.setDescription("For testing purposes only");
        deviceType2.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    public void testDeviceTypeCreationWithoutName() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(null, this.deviceProtocolPluggableClass);

        // Business method
        deviceType.save();

        // Asserts: See ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    public void testDeviceTypeCreationWithEmptyName() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("", this.deviceProtocolPluggableClass);

        // Business method
        deviceType.save();

        // Asserts: See the ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    public void testDeviceTypeCreationWithTooLongAName() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(longUnicodeString(81), this.deviceProtocolPluggableClass);

        // Business method
        deviceType.save();

        // Asserts: See the ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "description")
    public void testDeviceTypeCreationWithTooLongADescription() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(longUnicodeString(80), this.deviceProtocolPluggableClass);
        deviceType.setDescription(longUnicodeString(4001));

        // Business method
        deviceType.save();

        // Asserts: See the ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_PROTOCOL_IS_REQUIRED + "}")
    public void testDeviceTypeCreationWithoutProtocol() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("testDeviceTypeCreationWithoutProtocol", (DeviceProtocolPluggableClass) null);

        // Business method
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

        // Business method
        deviceType.addLogBookType(this.logBookType2);
        deviceType.save();

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
        deviceType.save();

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

        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

        // Business method
        deviceType.addLoadProfileType(this.loadProfileType2);
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

        // Asserts
        assertThat(deviceType.getRegisterTypes()).containsOnly(this.registerType1);
    }

    @Test
    @Transactional
    public void testUpdateDeviceTypeWithConfigWithSameProtocolDoesNotDetectChange() {
        // JP-1845
        String deviceTypeName = "testCreateDeviceTypeWithRegisterType";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);
        deviceType.save();

        deviceType.newConfiguration("first").description("at least one").add();

        deviceType = this.reloadCreatedDeviceType(deviceType.getId());
        // Business method
        deviceType.setDeviceProtocolPluggableClass(deviceProtocolPluggableClass);
        deviceType.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS + "}")
    public void testUpdateDeviceTypeWithConfigWithOtherProtocolDoesDetectChange() {
        // JP-1845
        String deviceTypeName = "testCreateDeviceTypeWithRegisterType";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);
        deviceType.save();

        deviceType.newConfiguration("first").description("at least one").add();

        deviceType = this.reloadCreatedDeviceType(deviceType.getId());
        // Business method
        deviceType.setDeviceProtocolPluggableClass(deviceProtocolPluggableClass2);
        deviceType.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_PROTOCOL_IS_REQUIRED + "}", strict=false)
    public void testUpdateDeviceTypeWithConfigSetNullProtocol() {
        // JP-1845
        String deviceTypeName = "testCreateDeviceTypeWithRegisterType";
        DeviceType deviceType;
        this.setupRegisterTypesInExistingTransaction();

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addRegisterType(this.registerType1);
        deviceType.save();

        deviceType.newConfiguration("first").description("at least one").add();

        deviceType = this.reloadCreatedDeviceType(deviceType.getId());

        // Business method
        deviceType.setDeviceProtocolPluggableClass((DeviceProtocolPluggableClass) null);
        deviceType.save();
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
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

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
        deviceType.save();

        // Add DeviceConfiguration with a RegisterSpec that uses the RegisterType
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Conf 1 for " + deviceTypeName);
        NumericalRegisterSpec.Builder registerSpecBuilder = deviceConfigurationBuilder.newNumericalRegisterSpec(this.registerType1);
        registerSpecBuilder.setNumberOfDigits(5);
        registerSpecBuilder.setNumberOfFractionDigits(2);
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
        deviceType.save();
        deviceConfiguration.activate();

        // Business method
        deviceType.setDeviceProtocolPluggableClass(this.deviceProtocolPluggableClass2);
        deviceType.save();

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    public void isLogicalSlaveDelegatesToDeviceProtocolClass() throws SQLException, BusinessException {
        String deviceTypeName = "isLogicalSlaveDelegatesToDeviceProtocolClass";
        DeviceType deviceType;
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.save();

        // Business method
        deviceType.isLogicalSlave();

        // Asserts
        verify(this.deviceProtocolPluggableClass).getDeviceProtocol();
    }

    @Test
    @Transactional
    public void isLogicalSlaveWhenProtocolClassSaysSo() throws SQLException, BusinessException {
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(new ArrayList<>(EnumSet.of(DeviceProtocolCapabilities.PROTOCOL_SLAVE)));
        String deviceTypeName = "isLogicalSlaveWhenProtocolClassSaysSo";
        DeviceType deviceType;
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.save();

        // Business method
        boolean isLogicalSlave = deviceType.isLogicalSlave();

        // Asserts
        assertThat(isLogicalSlave).isTrue();
    }

    @Test
    @Transactional
    public void isLogicalSlaveWhenProtocolClassHasMultipleCapabilities() throws SQLException, BusinessException {
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(new ArrayList<>(EnumSet.allOf(DeviceProtocolCapabilities.class)));
        String deviceTypeName = "isLogicalSlaveWhenProtocolClassHasMultipleCapabilities";
        DeviceType deviceType;
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(deviceTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.save();

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
        deviceType.save();
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
        deviceType.save();
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
        deviceType.save();

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
        deviceType.save();
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
        deviceType.save();
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
        deviceType.save();
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
    @ExpectedConstraintViolation(messageId = "{X.name.required}", property = "name")
    public void testCanNotAddDeviceConfigurationWithoutName() throws Exception {
        deviceType.newConfiguration(null).description("this is it!").add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.NAME_REQUIRED +"}", property = "name")
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

        ValidationRuleSet validationRuleSet = inMemoryPersistence.getValidationService().createValidationRuleSet("ValidationRuleSet");
        validationRuleSet.save();
        deviceConfiguration.addValidationRuleSet(validationRuleSet);

        EstimationRuleSet estimationRuleSet = inMemoryPersistence.getEstimationService().createEstimationRuleSet("EstimationRuleSet");
        estimationRuleSet.save();
        deviceConfiguration.addEstimationRuleSet(estimationRuleSet);

        deviceType.removeConfiguration(deviceConfiguration);
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