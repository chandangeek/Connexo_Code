package com.energyict.mdc.engine.impl.core;

import com.energyict.comserver.commands.common.CommonCommandImplTests;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.protocol.tasks.LoadProfilesTask;
import com.energyict.mdc.protocol.tasks.LogBooksTask;
import com.energyict.mdc.device.config.ProtocolTask;
import com.energyict.mdc.protocol.tasks.RegistersTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        verify(commandRoot).getLegacyLoadProfileLogBooksCommand(loadProfilesTask, logBooksTask, commandRoot, scheduledComTask);
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
        verify(commandRoot).getLegacyLoadProfileLogBooksCommand(loadProfilesTask, null, commandRoot, scheduledComTask);
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
        verify(commandRoot).getLegacyLoadProfileLogBooksCommand(null, logBooksTask, commandRoot, scheduledComTask);
    }
}
