/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.impl.meterdata.DeviceCommandFactory;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceCommandFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (09:02)
 */
@RunWith(MockitoJUnitRunner.class)
public class EndDeviceCommandFactoryImplTest {

    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComTaskExecutionSessionBuilder builder;

    @Test
    public void testForEmptyList () {
        DeviceCommandFactory factory = new DeviceCommandFactoryImpl(null);

        // Business method
        List<DeviceCommand> deviceCommands = factory.newForAll(new ArrayList<>(0), serviceProvider);

        // Asserts
        assertThat(deviceCommands).isNotNull();
        assertThat(deviceCommands).isEmpty();
    }

    @Test
    public void testConvertMethodIsCalled () {
        DeviceCommandFactory factory = new DeviceCommandFactoryImpl(null);
        ServerCollectedData collectedData = mockCollectedData();

        // Business method
        List<DeviceCommand> deviceCommands = factory.newForAll(Collections.singletonList(collectedData), serviceProvider);

        // Asserts
        assertThat(deviceCommands).isNotNull();
        assertThat(deviceCommands).hasSize(2);
        verify(collectedData).toDeviceCommand(any(MeterDataStoreCommandImpl.class), eq(serviceProvider));
    }

    @Test
    public void testWithMultipleCollectedData () {
        DeviceCommandFactory factory = new DeviceCommandFactoryImpl(null);
        DeviceCommand deviceCommand1 = mock(DeviceCommand.class);
        ServerCollectedData collectedData1 = mockCollectedData(deviceCommand1);
        when(collectedData1.toDeviceCommand(any(MeterDataStoreCommandImpl.class), eq(serviceProvider))).thenReturn(deviceCommand1);
        DeviceCommand deviceCommand2 = mock(DeviceCommand.class);
        ServerCollectedData collectedData2 = mockCollectedData(deviceCommand2);
        when(collectedData2.toDeviceCommand(any(MeterDataStoreCommandImpl.class), eq(serviceProvider))).thenReturn(deviceCommand2);
        DeviceCommand deviceCommand3 = mock(DeviceCommand.class);
        ServerCollectedData collectedData3 = mockCollectedData(deviceCommand3);
        when(collectedData3.toDeviceCommand(any(MeterDataStoreCommandImpl.class), eq(serviceProvider))).thenReturn(deviceCommand3);

        // Business method
        List<DeviceCommand> deviceCommands = factory.newForAll(Arrays.asList(collectedData1, collectedData2, collectedData3), serviceProvider);

        // Asserts
        assertThat(deviceCommands).isNotNull();
        assertThat(deviceCommands).contains(deviceCommand1, deviceCommand2, deviceCommand3);
    }

    private ServerCollectedData mockCollectedData () {
        DeviceCommand deviceCommand = mock(DeviceCommand.class);
        return this.mockCollectedData(deviceCommand);
    }

    private ServerCollectedData mockCollectedData (DeviceCommand deviceCommand) {
        ServerCollectedData mock = mock(ServerCollectedData.class);
        MeterDataStoreCommandImpl meterDataStoreCommand = mock(MeterDataStoreCommandImpl.class);
        when(mock.toDeviceCommand(meterDataStoreCommand, serviceProvider)).thenReturn(deviceCommand);
        return mock;
    }

}