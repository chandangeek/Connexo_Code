package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LogBookSpecImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/02/14
 * Time: 11:22
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookSpecImplTest {

    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final String DEVICE_TYPE_NAME = LogBookSpecImplTest.class.getName() + "Type";
    private static final String DEVICE_CONFIGURATION_NAME = LogBookSpecImplTest.class.getName() + "Config";
    private static final String LOGBOOK_TYPE_NAME = LogBookSpecImplTest.class.getName() + "LogBookType";

    private final ObisCode logBookTypeObisCode = ObisCode.fromString("0.0.99.98.0.255");
    private final ObisCode overruledLogBookSpecObisCode = ObisCode.fromString("1.0.99.97.0.255");

    private InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    private DeviceType deviceType;
    private DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder;
    private DeviceConfiguration deviceConfiguration;
    private LogBookType logBookType;

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase("LogBookSpecImplTest.mdc.device.config");
        this.initializeMocks();
        this.initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration() {
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {

            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME, logBookTypeObisCode);
            logBookType.save();

            // Business method
            deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For logBookSpec Test purposes only");
            deviceType.addLogBookType(logBookType);
            deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
            deviceConfiguration = deviceConfigurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }
    }

    private void initializeMocks() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    private LogBookSpec createDefaultTestingLogBookSpecWithOverruledObisCode() {
        LogBookSpec logBookSpec;
        try(TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = deviceConfiguration.createLogBookSpec(this.logBookType);
            logBookSpecBuilder.setOverruledObisCode(overruledLogBookSpecObisCode);
            logBookSpec = logBookSpecBuilder.add();
            tctx.commit();
        }
        return logBookSpec;
    }

    @Test
    public void createLogBookSpecTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        assertThat(logBookSpec.getLogBookType()).isEqualTo(this.logBookType);
        assertThat(logBookSpec.getDeviceObisCode()).isEqualTo(overruledLogBookSpecObisCode);
        assertThat(logBookSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(logBookSpec.getObisCode()).isEqualTo(this.logBookTypeObisCode);
    }

    @Test
    public void updateLogBookSpecTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            logBookSpec.setOverruledObisCode(null);
            tctx.commit();
        }

    }
}
