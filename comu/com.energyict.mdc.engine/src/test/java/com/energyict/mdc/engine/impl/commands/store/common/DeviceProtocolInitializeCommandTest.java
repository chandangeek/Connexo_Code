package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link DeviceProtocolInitializeCommand} component
 *
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 14:50
 */
public class DeviceProtocolInitializeCommandTest extends AbstractComCommandExecuteTest {

    private ComChannelPlaceHolder getMockedComChannel(){
        return ComChannelPlaceHolder.forKnownComChannel(mock(ComPortRelatedComChannel.class));
    }

    @Test
    public void comCommandTypeTest(){
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, this.newTestExecutionContext(), this.commandRootServiceProvider);
        DeviceProtocolInitializeCommand deviceProtocolInitializeCommand = new DeviceProtocolInitializeCommand(commandRoot, offlineDevice, getMockedComChannel());

        assertEquals(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE, deviceProtocolInitializeCommand.getCommandType());
    }

    @Test
    public void verifyInitTest(){
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, this.commandRootServiceProvider);
        ComChannelPlaceHolder comChannelPlaceHolder = getMockedComChannel();
        ComChannel mockedComChannel = comChannelPlaceHolder.getComPortRelatedComChannel();
        CommandFactory.createDeviceProtocolInitialization(commandRoot, null, offlineDevice, comChannelPlaceHolder);

        // business call
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the deviceProtocol.init method gets called
        verify(deviceProtocol).init(offlineDevice, mockedComChannel);
    }

}