package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.access.LogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOnCommand;
import com.energyict.mdc.engine.impl.commands.store.common.AddPropertiesCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolInitializeCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolSetCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolTerminateCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolUpdateCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.legacy.HandHeldUnitEnablerCommand;
import com.energyict.mdc.engine.impl.commands.store.legacy.InitializeLoggerCommand;
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
import com.energyict.protocols.mdc.channels.serial.SerialComChannel;
import com.energyict.protocols.mdc.channels.serial.ServerSerialPort;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author sva
 * @since 19/12/12 - 15:19
 */
@RunWith(MockitoJUnitRunner.class)
public class LegacySmartMeterProtocolCommandCreatorTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;

    private final FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private CommandRoot.ServiceProvider commandRootServiceProvider = new CommandRootServiceProviderAdapter(serviceProvider);

    @Mock
    private IssueService issueService;
    @Mock
    private Clock clock;
    @Mock
    private TaskHistoryService taskHistoryService;

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

    private ComTaskExecutionConnectionSteps createSingleDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(ComTaskExecutionConnectionSteps.SIGNON, ComTaskExecutionConnectionSteps.SIGNOFF);
    }

    @Test
    public void testCommandCreationOrder() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot root = spy(new CommandRootImpl(device, this.newTestExecutionContext(), commandRootServiceProvider));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTasks()).thenReturn(Arrays.asList(comTask));
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createSingleDeviceComTaskExecutionSteps();
        LegacySmartMeterProtocolCommandCreator commandCreator = new LegacySmartMeterProtocolCommandCreator();
        commandCreator.createCommands(
                            root,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                            device,
                Collections.<ProtocolTask>emptyList(),
                null, comTaskExecutionConnectionStep, null, issueService);

        InOrder order = Mockito.inOrder(root);
        order.verify(root).addCommand(any(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(InitializeLoggerCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(AddPropertiesCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(LogOnCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
    }

    @Test
    public void testCommandCreationOrderWithOptical() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot root = spy(new CommandRootImpl(device, this.newTestExecutionContext(), commandRootServiceProvider));

        SerialComChannel serialComChannel = mock(SerialComChannel.class);
        ServerSerialPort serverSerialPort = mock(ServerSerialPort.class);
        when(serialComChannel.getSerialPort()).thenReturn(serverSerialPort);
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTasks()).thenReturn(Arrays.asList(comTask));
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createSingleDeviceComTaskExecutionSteps();

        LegacySmartMeterProtocolCommandCreator commandCreator = new LegacySmartMeterProtocolCommandCreator();
        commandCreator.createCommands(
                            root,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                            device,
                Collections.<ProtocolTask>emptyList(),
                null, comTaskExecutionConnectionStep, null, issueService);

        InOrder order = Mockito.inOrder(root);
        order.verify(root).addCommand(any(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(InitializeLoggerCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(AddPropertiesCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(HandHeldUnitEnablerCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(LogOnCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(root).addCommand(any(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
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