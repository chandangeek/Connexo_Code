package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.impl.ServerPartialOutboundConnectionTask;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
    private ServerPartialOutboundConnectionTask outboundConnectionTask;

    private DeviceConfiguration createDeviceConfigurationWithConnectionType() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithRegisterMappings");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        DeviceCommunicationConfiguration communicationConfiguration = inMemoryPersistence.getDeviceConfigurationService().newDeviceCommunicationConfiguration(deviceConfiguration);
        communicationConfiguration.save();
        outboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
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

}
