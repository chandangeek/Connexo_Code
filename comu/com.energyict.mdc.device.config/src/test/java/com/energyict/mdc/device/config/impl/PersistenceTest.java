package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceTest {

    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    DeviceProtocol deviceProtocol;
    List<DeviceMessageSpec> deviceMessageIds;
    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;

    public PersistenceTest() {
    }

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabaseWithMockedProtocolPluggableService("PersistenceTest.mdc.device.config", false);
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
        deviceMessageIds = new ArrayList<>();
        DeviceMessageSpec deviceMessageSpec1 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.CONTACTOR_CLOSE.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);
        DeviceMessageSpec deviceMessageSpec2 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec2.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN.dbValue());
        deviceMessageIds.add(deviceMessageSpec2);
        DeviceMessageSpec deviceMessageSpec3 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec3.getId()).thenReturn(DeviceMessageId.CONTACTOR_ARM.dbValue());
        deviceMessageIds.add(deviceMessageSpec3);

        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel encryptionAccessLevel = mock(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(inMemoryPersistence.getProtocolPluggableService()
                .findDeviceProtocolPluggableClass(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID)).thenReturn(Optional.of(this.deviceProtocolPluggableClass));
    }
}