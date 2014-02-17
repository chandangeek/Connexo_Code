package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class CommonDeviceConfigSpecsTest {

    static final String DEVICE_TYPE_NAME = LogBookSpecImplTest.class.getName() + "Type";
    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    protected DeviceType deviceType;

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    public CommonDeviceConfigSpecsTest() {
    }

    @Before
    public void initialize() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase("CommonDeviceConfigSpecsTest.mdc.device.config");

        this.initializeMocks();

        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, this.deviceProtocolPluggableClass);
            deviceType.save();
            tctx.commit();
        }
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    private void initializeMocks() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
    }
}