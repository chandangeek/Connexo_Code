package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.TimeZone;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:04)
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceIntegrationTest {

    static final String DEVICE_TYPE_NAME = PersistenceIntegrationTest.class.getName() + "Type";
    static final String DEVICE_CONFIGURATION_NAME = PersistenceIntegrationTest.class.getName() + "Config";
    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    protected static final String MRID = "MyUniqueMRID";

    protected static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    protected DeviceType deviceType;
    protected DeviceConfiguration deviceConfiguration;
    protected SecurityPropertySet securityPropertySet;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    protected static Clock clock = mock(Clock.class);
    protected static InMemoryIntegrationPersistence inMemoryPersistence;

    EnumSet<DeviceMessageId> deviceMessageIds;

    @BeforeClass
    public static void initialize() throws SQLException {
        initializeClock();
        inMemoryPersistence = new InMemoryIntegrationPersistence(clock);
        inMemoryPersistence.initializeDatabase("PersistenceIntegrationTest.mdc.device.topology", false);
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Before
    public void initializeMocks() {
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        deviceMessageIds = EnumSet.of(DeviceMessageId.CONTACTOR_CLOSE,
                DeviceMessageId.CONTACTOR_OPEN,
                DeviceMessageId.CONTACTOR_ARM,
                DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT,
                DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE,
                DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS);
        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        int anySecurityLevel = 0;
        when(authenticationAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
        when(clock.getZone()).thenReturn(utcTimeZone.toZoneId());
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(0L));  // Create DeviceType as early as possible to support unit tests that go back in time
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        deviceType.save();
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfigurationBuilder.isDirectlyAddressable(true);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceMessageIds.stream().forEach(deviceConfiguration::createDeviceMessageEnablement);
        deviceConfiguration.activate();
        SecurityPropertySetBuilder securityPropertySetBuilder = deviceConfiguration.createSecurityPropertySet("No Security");
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4);
        securityPropertySetBuilder.authenticationLevel(anySecurityLevel);
        securityPropertySetBuilder.encryptionLevel(anySecurityLevel);
        this.securityPropertySet = securityPropertySetBuilder.build();
        this.resetClock();
    }

    @After
    public void resetClock () {
        initializeClock();
    }

    private static void initializeClock() {
        when(clock.getZone()).thenReturn(utcTimeZone.toZoneId());
        when(clock.instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    protected Device createSimpleDevice() {
        return createSimpleDeviceWithName(this.getClass().getSimpleName());
    }

    protected Device createSimpleDeviceWithName(String name, String mRID){
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

    protected Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, "SimpleMrId");
    }

    protected Device getReloadedDevice(Device device) {
        return inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
    }

}