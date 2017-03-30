/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceTestWithMockedDeviceProtocol {

    static final String DEVICE_TYPE_NAME = PersistenceTestWithMockedDeviceProtocol.class.getName() + "Type";
    static final String DEVICE_CONFIGURATION_NAME = PersistenceTestWithMockedDeviceProtocol.class.getName() + "Config";
    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    static final Instant januaryFirst = Instant.ofEpochSecond(1451606400L);
    static final Instant firstInterval = januaryFirst;
    static final Instant secondInterval = firstInterval.plusSeconds(900);
    static final Instant thirdInterval = secondInterval.plusSeconds(900);

    protected DeviceType deviceType;
    protected DeviceConfiguration deviceConfiguration;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    DeviceProtocol deviceProtocol;

    static InMemoryPersistenceWithMockedDeviceProtocol inMemoryPersistence;

    public PersistenceTestWithMockedDeviceProtocol() {
    }

    @BeforeClass
    public static void initialize() {
        Clock clock = mock(Clock.class);
        when(clock.instant()).thenReturn(januaryFirst);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        inMemoryPersistence = new InMemoryPersistenceWithMockedDeviceProtocol(clock);
        inMemoryPersistence.initializeDatabase("PersistenceTest.mdc.device.data", false);
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
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.empty());
        when(inMemoryPersistence.getProtocolPluggableService().findDeviceProtocolPluggableClass(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID)).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(inMemoryPersistence.getMockProtocolPluggableService().getMockedProtocolPluggableService().findDeviceProtocolPluggableClass(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID)).thenReturn(Optional.of(deviceProtocolPluggableClass));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();
        IssueStatus wontFix = mock(IssueStatus.class);
        when(inMemoryPersistence.getIssueService().findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));
    }

    protected Device getReloadedDevice(Device device) {
        return inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
    }
}
