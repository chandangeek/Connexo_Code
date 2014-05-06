package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.comserver.commands.AbstractComCommandExecuteTest;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.comserver.core.CommandFactory;
import com.energyict.comserver.core.JobExecution;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import org.junit.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link DeviceProtocolUpdateCacheCommand} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/09/12
 * Time: 15:09
 */
public class DeviceProtocolUpdateCacheCommandTest extends AbstractComCommandExecuteTest {

    @Test
    public void comCommandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), issueService);
        DeviceProtocolUpdateCacheCommand updateCacheCommand = new DeviceProtocolUpdateCacheCommand(offlineDevice, commandRoot);

        assertEquals(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND, updateCacheCommand.getCommandType());
    }

    @Test
    public void validateUpdateCacheTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        JobExecution.ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, issueService);
        CommandFactory.createUpdateDeviceCacheCommand(commandRoot, null, offlineDevice);

        // business method
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the deviceProtocol.terminate gets called
        verify(deviceProtocol).getDeviceCache();
    }

}