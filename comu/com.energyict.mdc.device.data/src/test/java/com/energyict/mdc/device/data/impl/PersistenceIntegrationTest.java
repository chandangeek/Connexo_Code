package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 05/03/14
 * Time: 13:55
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceIntegrationTest {

    protected static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    static final String DEVICE_TYPE_NAME = PersistenceIntegrationTest.class.getName() + "Type";
    static final String DEVICE_CONFIGURATION_NAME = PersistenceIntegrationTest.class.getName() + "Config";
    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    protected static DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    protected static DeviceProtocol deviceProtocol;
    protected static InMemoryIntegrationPersistence inMemoryPersistence;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    protected DeviceType deviceType;
    protected DeviceConfiguration deviceConfiguration;
    protected SecurityPropertySet securityPropertySet;
    List<DeviceMessageSpec> deviceMessageIds;
    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;

    @BeforeClass
    public static void initialize() throws SQLException {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        initializeClock();
        inMemoryPersistence.initializeDatabase("PersistenceIntegrationTest.mdc.device.data", false);
        deviceProtocol = mock(DeviceProtocol.class);
        deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getCustomPropertySet()).thenReturn(Optional.empty());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    private static void initializeClock() {
        when(inMemoryPersistence.getClock().getZone()).thenReturn(utcTimeZone.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @Before
    public void initializeMocks() {
        deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.CONTACTOR_CLOSE.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec2 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec2.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN.dbValue());
        deviceMessageIds.add(deviceMessageSpec2);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec3 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec3.getId()).thenReturn(DeviceMessageId.CONTACTOR_ARM.dbValue());
        deviceMessageIds.add(deviceMessageSpec3);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec4 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec4.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT.dbValue());
        deviceMessageIds.add(deviceMessageSpec4);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec5 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec5.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec5);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec6 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec6.getId()).thenReturn(DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS.dbValue());
        deviceMessageIds.add(deviceMessageSpec6);

        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class);
        int anySecurityLevel = 0;
        when(authenticationAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.singletonList(authenticationAccessLevel));
        com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel encryptionAccessLevel = mock(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(encryptionAccessLevel));
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
        freezeClock(1970, Calendar.JANUARY, 1); // Experiencing timing issues in tests that set clock back in time and the respective devices need their device life cycle
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfigurationBuilder.isDirectlyAddressable(true);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceMessageIds.forEach(deviceConfiguration::createDeviceMessageEnablement);
        deviceConfiguration.activate();
        SecurityPropertySetBuilder securityPropertySetBuilder = deviceConfiguration.createSecurityPropertySet("No Security");
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
    public void resetClock() {
        initializeClock();
    }

    @After
    public void clearMultiplierTypeCache() {
        inMemoryPersistence.getDeviceService().clearMultiplierTypeCache();
    }

    protected Instant freezeClock(int year, int month, int day) {
        return freezeClock(year, month, day, 0, 0, 0, 0);
    }

    protected Instant freezeClock(int year, int month, int day, TimeZone timeZone) {
        return freezeClock(year, month, day, 0, 0, 0, 0, timeZone);
    }

    protected Instant freezeClock(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        return freezeClock(year, month, day, hour, minute, second, millisecond, utcTimeZone);
    }

    protected Instant freezeClock(int year, int month, int day, int hour, int minute, int second, int millisecond, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        when(inMemoryPersistence.getClock().getZone()).thenReturn(timeZone.toZoneId());
        Instant frozenClockValue = calendar.getTime().toInstant();
        when(inMemoryPersistence.getClock().instant()).thenReturn(frozenClockValue);
        return frozenClockValue;
    }

    protected Device getReloadedDevice(Device device) {
        return inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
    }

}