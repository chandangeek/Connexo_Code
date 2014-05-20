package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link DeviceProtocolTerminateCommand} component
 *
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 14:56
 */
public class DeviceProtocolTerminateCommandTest extends AbstractComCommandExecuteTest {

    @Test
    public void commandTypeTest(){
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), serviceProvider);
        DeviceProtocolTerminateCommand deviceProtocolTerminateCommand = new DeviceProtocolTerminateCommand(commandRoot);

        assertEquals(ComCommandTypes.DEVICE_PROTOCOL_TERMINATE, deviceProtocolTerminateCommand.getCommandType());
    }

    @Test
    public void validateTerminateCallTest(){
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, serviceProvider);
        CommandFactory.createDeviceProtocolTerminate(commandRoot, null);

        // business method
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the deviceProtocol.terminate gets called
        verify(deviceProtocol).terminate();
    }

}