/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeviceProtocolUpdateCacheCommandTest extends AbstractComCommandExecuteTest {

    @Test
    public void comCommandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        DeviceProtocolUpdateCacheCommand updateCacheCommand = new DeviceProtocolUpdateCacheCommand(groupedDeviceCommand);

        assertEquals(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND, updateCacheCommand.getCommandType());
    }

    @Test
    public void validateUpdateCacheTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        CommandFactory.createUpdateDeviceCacheCommand(groupedDeviceCommand, comTaskExecution, offlineDevice);

        // business method
        groupedDeviceCommand.execute(executionContext);
        ComCommand cacheCommand = groupedDeviceCommand.getComCommand(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
        String journalEntry = cacheCommand.toJournalMessageDescription(LogLevel.DEBUG);

        // verify that the deviceProtocol.terminate gets called
        verify(deviceProtocol).getDeviceCache();
        assertEquals("Update the stored device cache {No update needed}", journalEntry);
    }

}