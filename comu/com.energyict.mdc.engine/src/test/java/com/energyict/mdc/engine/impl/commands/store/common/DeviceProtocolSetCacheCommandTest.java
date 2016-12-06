package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link DeviceProtocolSetCacheCommand} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/09/12
 * Time: 14:18
 */
public class DeviceProtocolSetCacheCommandTest extends AbstractComCommandExecuteTest {

    @Test
    public void comCommandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        DeviceProtocolSetCacheCommand setCacheCommand = new DeviceProtocolSetCacheCommand(groupedDeviceCommand);

        assertEquals(ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND, setCacheCommand.getCommandType());
    }

    @Test
    public void validateSetCacheTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        CommandFactory.createSetDeviceCacheCommand(groupedDeviceCommand, comTaskExecution, offlineDevice);

        // business method
        groupedDeviceCommand.execute(executionContext);

        // verify that the deviceProtocol.terminate gets called
        verify(deviceProtocol).setDeviceCache(any(DeviceProtocolCache.class));
    }

}