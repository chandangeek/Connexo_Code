package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import org.junit.*;

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
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), issueService);
        DeviceProtocolInitializeCommand deviceProtocolInitializeCommand = new DeviceProtocolInitializeCommand(commandRoot, offlineDevice, getMockedComChannel());

        assertEquals(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE, deviceProtocolInitializeCommand.getCommandType());
    }

    @Test
    public void verifyInitTest(){
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        JobExecution.ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, issueService);
        ComChannelPlaceHolder comChannelPlaceHolder = getMockedComChannel();
        ComChannel mockedComChannel = comChannelPlaceHolder.getComChannel();
        CommandFactory.createDeviceProtocolInitialization(commandRoot, null, offlineDevice, comChannelPlaceHolder);

        // business call
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the deviceProtocol.init method gets called
        verify(deviceProtocol).init(offlineDevice, mockedComChannel);
    }

}