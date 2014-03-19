package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 05/03/14
 * Time: 13:55
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceTest {

    static final String DEVICE_TYPE_NAME = PersistenceTest.class.getName() + "Type";
    static final String DEVICE_CONFIGURATION_NAME = PersistenceTest.class.getName() + "Config";
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

    static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    public PersistenceTest() {
    }

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabase("PersistenceTest.mdc.device.data", false, false);
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
        when(inMemoryPersistence.getProtocolPluggableService().findDeviceProtocolPluggableClass(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID)).thenReturn(deviceProtocolPluggableClass);
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
    }
}
