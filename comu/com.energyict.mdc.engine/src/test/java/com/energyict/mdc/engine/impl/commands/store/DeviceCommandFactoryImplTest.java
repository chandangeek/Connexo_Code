package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.meterdata.DeviceCommandFactory;
import com.energyict.mdc.meterdata.ServerCollectedData;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceCommandFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (09:02)
 */
public class DeviceCommandFactoryImplTest {

    @Test
    public void testForEmptyList () {
        DeviceCommandFactory factory = new DeviceCommandFactoryImpl();

        // Business method
        CompositeDeviceCommand compositeDeviceCommand = factory.newCompositeForAll(new ArrayList<ServerCollectedData>(0), ComServer.LogLevel.INFO, issueService);

        // Asserts
        Assertions.assertThat(compositeDeviceCommand).isNotNull();
        assertThat(compositeDeviceCommand.getChildren()).isEmpty();
    }

    @Test
    public void testConvertMethodIsCalled () {
        DeviceCommandFactory factory = new DeviceCommandFactoryImpl();
        ServerCollectedData collectedData = mockCollectedData();

        // Business method
        CompositeDeviceCommand compositeDeviceCommand = factory.newCompositeForAll(Arrays.asList(collectedData), com.energyict.mdc.engine.model.ComServer.LogLevel.INFO, issueService);

        // Asserts
        Assertions.assertThat(compositeDeviceCommand).isNotNull();
        assertThat(compositeDeviceCommand.getChildren()).hasSize(1);
        verify(collectedData).toDeviceCommand(issueService);
    }

    @Test
    public void testWithMultipleCollectedData () {
        DeviceCommandFactory factory = new DeviceCommandFactoryImpl();
        DeviceCommand deviceCommand1 = mock(DeviceCommand.class);
        ServerCollectedData collectedData1 = mockCollectedData(deviceCommand1);
        when(collectedData1.toDeviceCommand(issueService)).thenReturn(deviceCommand1);
        DeviceCommand deviceCommand2 = mock(DeviceCommand.class);
        ServerCollectedData collectedData2 = mockCollectedData(deviceCommand2);
        when(collectedData2.toDeviceCommand(issueService)).thenReturn(deviceCommand2);
        DeviceCommand deviceCommand3 = mock(DeviceCommand.class);
        ServerCollectedData collectedData3 = mockCollectedData(deviceCommand3);
        when(collectedData3.toDeviceCommand(issueService)).thenReturn(deviceCommand3);

        // Business method
        CompositeDeviceCommand compositeDeviceCommand = factory.newCompositeForAll(Arrays.asList(collectedData1, collectedData2, collectedData3), com.energyict.mdc.engine.model.ComServer.LogLevel.INFO, issueService);

        // Asserts
        Assertions.assertThat(compositeDeviceCommand).isNotNull();
        assertThat(compositeDeviceCommand.getChildren()).containsOnly(deviceCommand1, deviceCommand2, deviceCommand3);
    }

    private ServerCollectedData mockCollectedData () {
        DeviceCommand deviceCommand = mock(DeviceCommand.class);
        return this.mockCollectedData(deviceCommand);
    }

    private ServerCollectedData mockCollectedData (DeviceCommand deviceCommand) {
        ServerCollectedData mock = mock(ServerCollectedData.class);
        when(mock.toDeviceCommand(issueService)).thenReturn(deviceCommand);
        return mock;
    }

}