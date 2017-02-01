/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author sva
 * @since 19/12/12 - 9:38
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandFactoryTest extends CommonCommandImplTests {

    @Mock
    private ComTaskExecution scheduledComTask;
    @Mock
    private ComTask comTask;
    @Mock
    private OfflineDevice offlineDevice;

    @Test
    public void createLegacyCommandsFromTaskTest() throws Exception {
        RegistersTask registersTask = mock(RegistersTask.class);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LogBooksTask logBooksTask = mock(LogBooksTask.class);

        List<ProtocolTask> protocolTasks = new ArrayList<>();
        protocolTasks.add(registersTask);
        protocolTasks.add(loadProfilesTask);
        protocolTasks.add(logBooksTask);

        GroupedDeviceCommand groupedDeviceCommand = mock(GroupedDeviceCommand.class);
        // Business methods
        CommandFactory.createLegacyCommandsFromTask(groupedDeviceCommand, scheduledComTask, protocolTasks);

        // Asserts
        verify(groupedDeviceCommand).getLegacyLoadProfileLogBooksCommand(loadProfilesTask, logBooksTask, groupedDeviceCommand, scheduledComTask);
    }

    @Test
    public void createLegacyCommandsFromTaskHavingNoLogBooksTaskTest() throws Exception {
        RegistersTask registersTask = mock(RegistersTask.class);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);

        List<ProtocolTask> protocolTasks = new ArrayList<>();
        protocolTasks.add(registersTask);
        protocolTasks.add(loadProfilesTask);

        GroupedDeviceCommand groupedDeviceCommand = mock(GroupedDeviceCommand.class);

        // Business methods
        CommandFactory.createLegacyCommandsFromTask(groupedDeviceCommand, scheduledComTask, protocolTasks);

        // Asserts
        verify(groupedDeviceCommand).getLegacyLoadProfileLogBooksCommand(loadProfilesTask, null, groupedDeviceCommand, scheduledComTask);
    }

    @Test
    public void createLegacyCommandsFromTaskHavingNoLoadProfilesTaskTest() throws Exception {
        RegistersTask registersTask = mock(RegistersTask.class);
        LogBooksTask logBooksTask = mock(LogBooksTask.class);

        List<ProtocolTask> protocolTasks = new ArrayList<>();
        protocolTasks.add(registersTask);
        protocolTasks.add(logBooksTask);

        GroupedDeviceCommand groupedDeviceCommand = mock(GroupedDeviceCommand.class);

        // Business methods
        CommandFactory.createLegacyCommandsFromTask(groupedDeviceCommand, scheduledComTask, protocolTasks);

        // Asserts
        verify(groupedDeviceCommand).getLegacyLoadProfileLogBooksCommand(null, logBooksTask, groupedDeviceCommand, scheduledComTask);
    }
}