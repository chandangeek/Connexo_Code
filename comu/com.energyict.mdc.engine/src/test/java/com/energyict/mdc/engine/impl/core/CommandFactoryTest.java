package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;

import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
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
    private CommandRootImpl commandRoot;
    @Mock
    private ComTaskExecution scheduledComTask;
    @Mock
    private ComTask comTask;

    @Test
    public void createLegacyCommandsFromTaskTest() throws Exception {
        RegistersTask registersTask = mock(RegistersTask.class);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LogBooksTask logBooksTask = mock(LogBooksTask.class);

        List<ProtocolTask> protocolTasks = new ArrayList<>();
        protocolTasks.add(registersTask);
        protocolTasks.add(loadProfilesTask);
        protocolTasks.add(logBooksTask);

        // Business methods
        CommandFactory.createLegacyCommandsFromTask(commandRoot, scheduledComTask, protocolTasks);

        // Asserts
        verify(commandRoot).findOrCreateLegacyLoadProfileLogBooksCommand(loadProfilesTask, logBooksTask, commandRoot, scheduledComTask);
    }

    @Test
    public void createLegacyCommandsFromTaskHavingNoLogBooksTaskTest() throws Exception {
        RegistersTask registersTask = mock(RegistersTask.class);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);

        List<ProtocolTask> protocolTasks = new ArrayList<>();
        protocolTasks.add(registersTask);
        protocolTasks.add(loadProfilesTask);

        // Business methods
        CommandFactory.createLegacyCommandsFromTask(commandRoot, scheduledComTask, protocolTasks);

        // Asserts
        verify(commandRoot).findOrCreateLegacyLoadProfileLogBooksCommand(loadProfilesTask, null, commandRoot, scheduledComTask);
    }

    @Test
    public void createLegacyCommandsFromTaskHavingNoLoadProfilesTaskTest() throws Exception {
        RegistersTask registersTask = mock(RegistersTask.class);
        LogBooksTask logBooksTask = mock(LogBooksTask.class);

        List<ProtocolTask> protocolTasks = new ArrayList<>();
        protocolTasks.add(registersTask);
        protocolTasks.add(logBooksTask);

        // Business methods
        CommandFactory.createLegacyCommandsFromTask(commandRoot, scheduledComTask, protocolTasks);

        // Asserts
        verify(commandRoot).findOrCreateLegacyLoadProfileLogBooksCommand(null, logBooksTask, commandRoot, scheduledComTask);
    }
}
