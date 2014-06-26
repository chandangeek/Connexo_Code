package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.access.DaisyChainedLogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.DaisyChainedLogOnCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOnCommand;
import com.energyict.mdc.engine.impl.commands.store.common.AddPropertiesCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolInitializeCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolSetCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolTerminateCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolUpdateCacheCommand;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionConnectionSteps;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.history.TaskHistoryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Tests the DeviceProtocolCommandCreator
 *
 * Copyrights EnergyICT
 * Date: 10/10/12
 * Time: 8:49
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolCommandCreatorTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;

    private FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private CommandRoot.ServiceProvider commandRootServiceProvider = new CommandRootServiceProviderAdapter(serviceProvider);

    @Mock
    private IssueService issueService;
    @Mock
    private Clock clock;
    @Mock
    private TaskHistoryService taskHistoryService;

    private ComTaskExecutionConnectionSteps createSingleDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(ComTaskExecutionConnectionSteps.SIGNON, ComTaskExecutionConnectionSteps.SIGNOFF);
    }

    private ComTaskExecutionConnectionSteps createMiddleDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(0);
    }

    private ComTaskExecutionConnectionSteps createLastDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(ComTaskExecutionConnectionSteps.SIGNOFF);
    }

    @Before
    public void initBefore() {
        serviceProvider.setClock(clock);
        serviceProvider.setTaskHistoryService(taskHistoryService);
    }

    @After
    public void initAfter() {
        serviceProvider.setClock(null);
        serviceProvider.setTaskHistoryService(null);
    }

    @Test
    public void testCommandCreationOrder(){
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot root = spy(new CommandRootImpl(device, this.newTestExecutionContext(), serviceProvider));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTasks()).thenReturn(Arrays.asList(comTask));
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createSingleDeviceComTaskExecutionSteps();

        DeviceProtocolCommandCreator commandCreator =new DeviceProtocolCommandCreator();
        commandCreator.createCommands(
                            root,
                            TypedProperties.empty(),
                            ComChannelPlaceHolder.forKnownComChannel(comChannel),
                            device,
                Collections.<ProtocolTask>emptyList(),
                null, comTaskExecutionConnectionStep, null, issueService);

        InOrder order = Mockito.inOrder(root);
        order.verify(root).addCommand(isA(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(isA(AddPropertiesCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(isA(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(isA(LogOnCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(isA(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(isA(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(isA(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
    }

    @Test
    public void testMiddleStateCreationOrder(){
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot root = spy(new CommandRootImpl(device, this.newTestExecutionContext(), serviceProvider));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTasks()).thenReturn(Arrays.asList(comTask));
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createMiddleDeviceComTaskExecutionSteps();

        DeviceProtocolCommandCreator commandCreator =new DeviceProtocolCommandCreator();
        commandCreator.createCommands(
                            root,
                            TypedProperties.empty(),
                            ComChannelPlaceHolder.forKnownComChannel(comChannel),
                            device,
                Collections.<ProtocolTask>emptyList(),
                null, comTaskExecutionConnectionStep, null, issueService);

        // none of the 'connection' related commands should be called ...
        verify(root, times(0)).addCommand(isA(LogOffCommand.class), any(ComTaskExecution.class));
        verify(root, times(0)).addCommand(isA(LogOnCommand.class), any(ComTaskExecution.class));
        verify(root, times(0)).addCommand(isA(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        verify(root, times(0)).addCommand(isA(AddPropertiesCommand.class), any(ComTaskExecution.class));
        verify(root, times(0)).addCommand(isA(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        verify(root, times(0)).addCommand(isA(DaisyChainedLogOnCommand.class), any(ComTaskExecution.class));
        verify(root, times(0)).addCommand(isA(DaisyChainedLogOffCommand.class), any(ComTaskExecution.class));
        verify(root, times(0)).addCommand(isA(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        verify(root, times(0)).addCommand(isA(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
    }

    @Test
    public void testLastStateCreationOrder() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot root = spy(new CommandRootImpl(device, this.newTestExecutionContext(), serviceProvider));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTasks()).thenReturn(Arrays.asList(comTask));
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createLastDeviceComTaskExecutionSteps();

        DeviceProtocolCommandCreator commandCreator =new DeviceProtocolCommandCreator();
        commandCreator.createCommands(
                            root,
                            TypedProperties.empty(),
                            ComChannelPlaceHolder.forKnownComChannel(comChannel),
                            device,
                Collections.<ProtocolTask>emptyList(),
                null, comTaskExecutionConnectionStep, null, issueService);

        InOrder order = Mockito.inOrder(root);
        order.verify(root).addCommand(isA(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(isA(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(isA(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));

        // the normal LogOn should never be called
        verify(root, times(0)).addCommand(isA(LogOnCommand.class), any(ComTaskExecution.class));
    }

    private ExecutionContext newTestExecutionContext () {
        return newTestExecutionContext(Logger.getAnonymousLogger());
    }

    private ExecutionContext newTestExecutionContext (Logger logger) {
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        ComPortPool comPortPool = mock(ComPortPool.class);
        when(comPortPool.getId()).thenReturn(COMPORT_POOL_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getComServer()).thenReturn(comServer);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        ExecutionContext executionContext =
                new ExecutionContext(
                        mock(JobExecution.class),
                        connectionTask,
                        comPort, commandRootServiceProvider);
        executionContext.setLogger(logger);
        return executionContext;
    }

}
