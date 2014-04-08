package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.NoParamsConnectionType;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 04/04/14
 * Time: 12:06
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceTestsForCommunication extends PersistenceIntegrationTest {

    private static final ComWindow COM_WINDOW = new ComWindow(3600, 7200);

    private OutboundComPortPool outboundComPortPool;
    private ConnectionTypePluggableClass connectionTypePluggableClass;
    private PartialOutboundConnectionTask partialOutboundConnectionTask;

    private DeviceConfiguration createDeviceConfigurationWithConnectionType() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithRegisterMappings");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        DeviceCommunicationConfiguration communicationConfiguration = inMemoryPersistence.getDeviceConfigurationService().newDeviceCommunicationConfiguration(deviceConfiguration);
        communicationConfiguration.save();
        partialOutboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
                .name("MyOutbound")
                .comPortPool(outboundComPortPool)
                .pluggableClass(connectionTypePluggableClass)
                .comWindow(COM_WINDOW)
                .rescheduleDelay(TimeDuration.seconds(60))
                .connectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .asDefault(true).build();
        communicationConfiguration.save();
        deviceType.save();
        return deviceConfiguration;
    }

    @Before
    public void initBefore() {
        connectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().newConnectionTypePluggableClass("NoParamsConnectionType", NoParamsConnectionType.class.getName());
        connectionTypePluggableClass.save();
        outboundComPortPool = inMemoryPersistence.getEngineModelService().newOutboundComPortPool();
        outboundComPortPool.setActive(true);
        outboundComPortPool.setComPortType(ComPortType.TCP);
        outboundComPortPool.setName("inboundComPortPool");
        outboundComPortPool.setTaskExecutionTimeout(TimeDuration.minutes(15));
        outboundComPortPool.save();
    }

    @Test
    @Transactional
    public void createDeviceWithoutConnectionTasksTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigurationWithConnectionType();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithoutConnectionTasks");
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();

        assertThat(connectionTasks).isEmpty();
    }

}
