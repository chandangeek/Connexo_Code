package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link DeviceProtocolInitializeCommand} component
 * <p>
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 14:50
 */
public class DeviceProtocolInitializeCommandTest extends AbstractComCommandExecuteTest {

    private ComChannelPlaceHolder getMockedComChannel() {
        return ComChannelPlaceHolder.forKnownComChannel(mock(ComPortRelatedComChannel.class));
    }

    @Test
    public void comCommandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        DeviceProtocolInitializeCommand deviceProtocolInitializeCommand = new DeviceProtocolInitializeCommand(groupedDeviceCommand, getMockedComChannel());

        assertEquals(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE, deviceProtocolInitializeCommand.getCommandType());
    }

    @Test
    public void verifyInitTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        ComChannelPlaceHolder comChannelPlaceHolder = getMockedComChannel();
        ComChannel mockedComChannel = comChannelPlaceHolder.getComPortRelatedComChannel();
        CommandFactory.createDeviceProtocolInitialization(groupedDeviceCommand, comTaskExecution, offlineDevice, comChannelPlaceHolder);

        // business call
        groupedDeviceCommand.execute(executionContext);

        // verify that the deviceProtocol.init method gets called
        verify(deviceProtocol).init(offlineDevice, mockedComChannel);
    }

}