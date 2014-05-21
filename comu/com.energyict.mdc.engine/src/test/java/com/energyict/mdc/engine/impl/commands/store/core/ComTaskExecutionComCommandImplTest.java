package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.meterdata.ComTaskExecutionCollectedData;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.tasks.ComTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ComTaskExecutionComCommandImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-18 (11:53)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionComCommandImplTest {

    private static final long COM_TASK_EXECUTION_ID = 97;
    private static final long COM_PORT_POOL_ID = COM_TASK_EXECUTION_ID + 1;

    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private CommandRoot commandRoot;
    private FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private CommandRoot.ServiceProvider commandRootServiceProvider = new CommandRootServiceProviderAdapter(serviceProvider);
    @Mock
    private TransactionService transactionService;

    @Before
    public void initializeMocks () {
        when(this.comTask.getName()).thenReturn(ComTaskExecutionComCommandImplTest.class.getSimpleName());
        when(this.comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(this.comTaskExecution.getComTask()).thenReturn(this.comTask);
        ComPort comPort = mock(ComPort.class);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comPort.getComServer()).thenReturn(comServer);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        ExecutionContext executionContext = new ExecutionContext(mock(JobExecution.class), connectionTask, comPort, commandRootServiceProvider);
        when(this.commandRoot.getExecutionContext()).thenReturn(executionContext);
    }

    @Test
    public void testGetCollectedDataWhenNone () {
        ComTaskExecutionComCommandImpl command = new ComTaskExecutionComCommandImpl(this.commandRoot, this.comTaskExecution);

        // Business method
        List<CollectedData> collectedDataList = command.getCollectedData();

        // Asserts
        assertThat(collectedDataList).hasSize(1);
        CollectedData collectedData = collectedDataList.get(0);
        assertThat(collectedData).isInstanceOf(ComTaskExecutionCollectedData.class);
    }

    @Test
    public void testGetCollectedDataIsCalled () {
        ComCommand comCommand1 = mock(ComCommand.class);
        when(comCommand1.getCommandType()).thenReturn(ComCommandTypes.BASIC_CHECK_COMMAND);
        ComCommand comCommand2 = mock(ComCommand.class);
        when(comCommand2.getCommandType()).thenReturn(ComCommandTypes.CLOCK_COMMAND);
        ComCommand comCommand3 = mock(ComCommand.class);
        when(comCommand3.getCommandType()).thenReturn(ComCommandTypes.READ_LOGBOOKS_COMMAND);
        ComTaskExecutionComCommandImpl command = new ComTaskExecutionComCommandImpl(this.commandRoot, this.comTaskExecution);
        command.addCommand(comCommand1, this.comTaskExecution);
        command.addCommand(comCommand2, this.comTaskExecution);
        command.addCommand(comCommand3, this.comTaskExecution);

        // Business method
        List<CollectedData> collectedDataList = command.getCollectedData();

        // Asserts
        assertThat(collectedDataList).hasSize(1);
        CollectedData collectedData = collectedDataList.get(0);
        assertThat(collectedData).isInstanceOf(ComTaskExecutionCollectedData.class);
        verify(comCommand1).getCollectedData();
        verify(comCommand2).getCollectedData();
        verify(comCommand3).getCollectedData();
    }

    @Test
    public void testGetCollectedData () {
        ComCommand comCommand1 = mock(ComCommand.class);
        when(comCommand1.getCommandType()).thenReturn(ComCommandTypes.BASIC_CHECK_COMMAND);
        ServerCollectedData collectedData1 = mock(ServerCollectedData.class);
        when(comCommand1.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(collectedData1));
        ComCommand comCommand2 = mock(ComCommand.class);
        when(comCommand2.getCommandType()).thenReturn(ComCommandTypes.CLOCK_COMMAND);
        ServerCollectedData collectedData2 = mock(ServerCollectedData.class);
        when(comCommand2.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(collectedData2));
        ComCommand comCommand3 = mock(ComCommand.class);
        when(comCommand3.getCommandType()).thenReturn(ComCommandTypes.READ_LOGBOOKS_COMMAND);
        ServerCollectedData collectedData3 = mock(ServerCollectedData.class);
        when(comCommand3.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(collectedData3));
        ComTaskExecutionComCommandImpl command = new ComTaskExecutionComCommandImpl(this.commandRoot, this.comTaskExecution);
        command.addCommand(comCommand1, this.comTaskExecution);
        command.addCommand(comCommand2, this.comTaskExecution);
        command.addCommand(comCommand3, this.comTaskExecution);

        // Business method
        List<CollectedData> collectedDataList = command.getCollectedData();

        // Asserts
        assertThat(collectedDataList).hasSize(1);
        CollectedData collectedData = collectedDataList.get(0);
        assertThat(collectedData).isInstanceOf(ComTaskExecutionCollectedData.class);
        ComTaskExecutionCollectedData comTaskExecutionCollectedData = (ComTaskExecutionCollectedData) collectedData;
        List<ServerCollectedData> actualCollectedData = comTaskExecutionCollectedData.getElements();
        assertThat(actualCollectedData).containsOnly(collectedData1, collectedData2, collectedData3);
    }

    @Test
    public void testContains () {
        ComCommand comCommand1 = mock(ComCommand.class);
        when(comCommand1.getCommandType()).thenReturn(ComCommandTypes.BASIC_CHECK_COMMAND);
        ComCommand comCommand2 = mock(ComCommand.class);
        when(comCommand2.getCommandType()).thenReturn(ComCommandTypes.CLOCK_COMMAND);
        ComCommand comCommand3 = mock(ComCommand.class);
        when(comCommand3.getCommandType()).thenReturn(ComCommandTypes.READ_LOGBOOKS_COMMAND);
        ComTaskExecutionComCommandImpl command = new ComTaskExecutionComCommandImpl(this.commandRoot, this.comTaskExecution);
        command.addCommand(comCommand1, this.comTaskExecution);
        command.addCommand(comCommand3, this.comTaskExecution);

        // Business method
        boolean contains1 = command.contains(comCommand1);
        boolean contains2 = command.contains(comCommand2);
        boolean contains3 = command.contains(comCommand3);

        // Asserts
        assertThat(contains1).isTrue();
        assertThat(contains2).isFalse();
        assertThat(contains3).isTrue();
    }

}