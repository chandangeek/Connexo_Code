package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.meterdata.DeviceCommandFactory;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DeviceCommandFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (09:02)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceCommandFactoryImplTest {

    @Mock
    private IssueService issueService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComTaskExecutionSessionBuilder builder;

    @Test
    public void testForEmptyList () {
        DeviceCommandFactory factory = new DeviceCommandFactoryImpl();

        // Business method
        CompositeDeviceCommand compositeDeviceCommand = factory.newCompositeForAll(new ArrayList<ServerCollectedData>(0), ComServer.LogLevel.INFO, issueService, builder);

        // Asserts
        assertThat(compositeDeviceCommand).isNotNull();
        assertThat(compositeDeviceCommand.getChildren()).isEmpty();
    }

    @Test
    public void testConvertMethodIsCalled () {
        DeviceCommandFactory factory = new DeviceCommandFactoryImpl();
        ServerCollectedData collectedData = mockCollectedData();

        // Business method
        CompositeDeviceCommand compositeDeviceCommand = factory.newCompositeForAll(Arrays.asList(collectedData), ComServer.LogLevel.INFO, issueService, builder);

        // Asserts
        assertThat(compositeDeviceCommand).isNotNull();
        assertThat(compositeDeviceCommand.getChildren()).hasSize(1);
        verify(collectedData).toDeviceCommand(issueService, meterDataStoreCommand);
    }

    @Test
    public void testWithMultipleCollectedData () {
        DeviceCommandFactory factory = new DeviceCommandFactoryImpl();
        DeviceCommand deviceCommand1 = mock(DeviceCommand.class);
        ServerCollectedData collectedData1 = mockCollectedData(deviceCommand1);
        when(collectedData1.toDeviceCommand(issueService, meterDataStoreCommand)).thenReturn(deviceCommand1);
        DeviceCommand deviceCommand2 = mock(DeviceCommand.class);
        ServerCollectedData collectedData2 = mockCollectedData(deviceCommand2);
        when(collectedData2.toDeviceCommand(issueService, meterDataStoreCommand)).thenReturn(deviceCommand2);
        DeviceCommand deviceCommand3 = mock(DeviceCommand.class);
        ServerCollectedData collectedData3 = mockCollectedData(deviceCommand3);
        when(collectedData3.toDeviceCommand(issueService, meterDataStoreCommand)).thenReturn(deviceCommand3);

        // Business method
        CompositeDeviceCommand compositeDeviceCommand = factory.newCompositeForAll(Arrays.asList(collectedData1, collectedData2, collectedData3), ComServer.LogLevel.INFO, issueService, builder);

        // Asserts
        assertThat(compositeDeviceCommand).isNotNull();
        assertThat(compositeDeviceCommand.getChildren()).containsOnly(deviceCommand1, deviceCommand2, deviceCommand3);
    }

    private ServerCollectedData mockCollectedData () {
        DeviceCommand deviceCommand = mock(DeviceCommand.class);
        return this.mockCollectedData(deviceCommand);
    }

    private ServerCollectedData mockCollectedData (DeviceCommand deviceCommand) {
        ServerCollectedData mock = mock(ServerCollectedData.class);
        when(mock.toDeviceCommand(issueService, meterDataStoreCommand)).thenReturn(deviceCommand);
        return mock;
    }

}