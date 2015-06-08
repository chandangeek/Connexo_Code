package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.time.Instant;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-11 (13:31)
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceIntegrationTest {

    private static final String DEVICE_TYPE_NAME = PersistenceIntegrationTest.class.getName() + "Type";
    private static final String DEVICE_CONFIGURATION_NAME = PersistenceIntegrationTest.class.getName() + "Config";

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    protected static DeviceType deviceType;
    protected static DeviceConfiguration deviceConfiguration;
    protected static InMemoryIntegrationPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() throws SQLException {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        inMemoryPersistence.initializeDatabase("PersistenceIntegrationTest.mdc.device.lifecycle", false);

        try (TransactionContext ctx = inMemoryPersistence.getTransactionService().getContext()) {
            ProtocolPluggableService protocolPluggableService = inMemoryPersistence.getProtocolPluggableService();
            protocolPluggableService.addDeviceProtocolService(new BareMinimumDeviceProtocolService());
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass("DLC-IntegrationTest", BareMinimumDeviceProtocol.class.getName());
            deviceProtocolPluggableClass.save();
            when(inMemoryPersistence.getClock().instant()).thenReturn(Instant.ofEpochMilli(0L));  // Create DeviceType as early as possible to support unit tests that go back in time
            deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
            deviceType.save();
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
            deviceConfigurationBuilder.isDirectlyAddressable(false);
            deviceConfiguration = deviceConfigurationBuilder.add();
            deviceConfiguration.activate();

            ctx.commit();
        }
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Before
    public void refreshDeviceTypeAndConfiguration() {
        DeviceConfigurationService service = inMemoryPersistence.getDeviceConfigurationService();
        deviceType = service.findDeviceType(deviceType.getId()).get();
        deviceConfiguration = service.findDeviceConfiguration(deviceConfiguration.getId()).get();
    }

    protected Device createSimpleDeviceWithName(String name, String mRID){
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

    private static class BareMinimumDeviceProtocolService implements DeviceProtocolService {
        @Override
        public Object createProtocol(String className) {
            if (BareMinimumDeviceProtocol.class.getName().equals(className)) {
                return new BareMinimumDeviceProtocol();
            }
            else {
                return null;
            }
        }
    }
}