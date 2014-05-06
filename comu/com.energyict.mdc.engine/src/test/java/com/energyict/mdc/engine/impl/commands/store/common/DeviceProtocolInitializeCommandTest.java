package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.comserver.commands.AbstractComCommandExecuteTest;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.comserver.core.CommandFactory;
import com.energyict.comserver.core.JobExecution;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.ComChannelPlaceHolder;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.ComPortRelatedComChannel;
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