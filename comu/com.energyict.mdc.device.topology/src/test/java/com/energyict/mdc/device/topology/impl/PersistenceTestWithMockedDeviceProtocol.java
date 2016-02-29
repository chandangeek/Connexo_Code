package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (17:18)
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceTestWithMockedDeviceProtocol {
    static final String DEVICE_TYPE_NAME = PersistenceTestWithMockedDeviceProtocol.class.getSimpleName() + "Type";
    static final String DEVICE_CONFIGURATION_NAME = PersistenceTestWithMockedDeviceProtocol.class.getSimpleName() + "Config";
    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
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

    static InMemoryPersistenceWithMockedDeviceProtocol inMemoryPersistence = new InMemoryPersistenceWithMockedDeviceProtocol();

    public PersistenceTestWithMockedDeviceProtocol() {
    }

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistenceWithMockedDeviceProtocol();
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
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(inMemoryPersistence.getProtocolPluggableService().findDeviceProtocolPluggableClass(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID)).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(inMemoryPersistence.getMockProtocolPluggableService().getMockedProtocolPluggableService().findDeviceProtocolPluggableClass(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID)).thenReturn(Optional.of(deviceProtocolPluggableClass));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();
    }

    protected Device getReloadedDevice(Device device) {
        return inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
    }

}