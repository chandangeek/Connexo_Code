/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectedFirmwareVersionDeviceCommandTest  {

    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;
    @Mock
    public ComServerDAO comServerDAO;

    @Test
    public void handOverToComServerDAOTest() {
        CollectedFirmwareVersion collectedFirmwareVersions = getSafeMockedCollectedFirmwareVersion();
        CollectedFirmwareVersionDeviceCommand collectedFirmwareVersionDeviceCommand = new CollectedFirmwareVersionDeviceCommand(serviceProvider, collectedFirmwareVersions, comTaskExecution);
        collectedFirmwareVersionDeviceCommand.doExecute(comServerDAO);

        //asserts
        verify(comServerDAO).updateFirmwareVersions(collectedFirmwareVersions);
    }

    public CollectedFirmwareVersion getSafeMockedCollectedFirmwareVersion() {
        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class);
        when(collectedFirmwareVersion.getActiveMeterFirmwareVersion()).thenReturn(Optional.empty());
        when(collectedFirmwareVersion.getActiveMeterFirmwareVersion()).thenReturn(Optional.empty());
        return collectedFirmwareVersion;
    }
}