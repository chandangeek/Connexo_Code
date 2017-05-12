/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceWithRealProtocolPluggableServiceTest {

    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
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
    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    DeviceProtocolDialect deviceProtocolDialect1, deviceProtocolDialect2;
    @Mock
    ProtocolDialectConfigurationProperties dialectConfigurationProperties1, dialectConfigurationProperties2;

    static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    public PersistenceWithRealProtocolPluggableServiceTest() {
    }

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabaseWithRealProtocolPluggableService("PersistenceTest.mdc.device.config", false);
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
        com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class);
        when(deviceProtocolDialect1.getDeviceProtocolDialectName()).thenReturn("DeviceProtocolDialect1");
        when(deviceProtocolDialect2.getDeviceProtocolDialectName()).thenReturn("DeviceProtocolDialect2");
        when(deviceProtocolDialect1.getDeviceProtocolDialectDisplayName()).thenReturn("Device Protocol dialect 1");
        when(deviceProtocolDialect2.getDeviceProtocolDialectDisplayName()).thenReturn("Device Protocol dialect 2");
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getClientSecurityPropertySpec()).thenReturn(Optional.empty());
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel encryptionAccessLevel = mock(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
        when(this.deviceProtocol.getDeviceProtocolDialects()).thenReturn(Arrays.asList(deviceProtocolDialect1, deviceProtocolDialect2));
    }

}