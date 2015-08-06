package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
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
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionConnectionSteps;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.ServerSerialPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 19/12/12 - 15:12
 */
@RunWith(MockitoJUnitRunner.class)
public class LegacyMeterProtocolCommandCreatorTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;

    @Mock
    private IssueService issueService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionTaskService connectionTaskService;
    @Mock
    private ExecutionContext.ServiceProvider serviceProvider;
    @Mock
    private CommandRoot.ServiceProvider commandRootServiceProvider;

    private ComTaskExecutionConnectionSteps createSingleDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(ComTaskExecutionConnectionSteps.SIGNON, ComTaskExecutionConnectionSteps.SIGNOFF);
    }

    @Before
    public void initializeMocks() {
        Clock clock = Clock.systemDefaultZone();
        DeviceService deviceService = mock(DeviceService.class);
        when(this.serviceProvider.clock()).thenReturn(clock);
        when(this.serviceProvider.deviceService()).thenReturn(deviceService);
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.commandRootServiceProvider.clock()).thenReturn(clock);
        when(this.commandRootServiceProvider.deviceService()).thenReturn(deviceService);
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

        LegacyMeterProtocolCommandCreator commandCreator = new LegacyMeterProtocolCommandCreator();
        commandCreator.createCommands(
                root,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                device,
                Collections.<ProtocolTask>emptyList(),
                null, comTaskExecutionConnectionStep, null, issueService);

        InOrder order = Mockito.inOrder(root);
        order.verify(root).addUniqueCommand(any(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(InitializeLoggerCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(AddPropertiesCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(LogOnCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
    }

    @Test
    public void testCommandCreationOrderWithOptical() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot root = spy(new CommandRootImpl(device, this.newTestExecutionContext(), commandRootServiceProvider));

        ServerSerialPort serverSerialPort = mock(ServerSerialPort.class);
        SerialComChannel serialComChannel = mock(SerialComChannel.class);
        when(serialComChannel.getSerialPort()).thenReturn(serverSerialPort);
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTasks()).thenReturn(Arrays.asList(comTask));
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createSingleDeviceComTaskExecutionSteps();

        LegacyMeterProtocolCommandCreator commandCreator = new LegacyMeterProtocolCommandCreator();
        commandCreator.createCommands(
                root,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                device,
                Collections.<ProtocolTask>emptyList(),
                null, comTaskExecutionConnectionStep, null, issueService);

        InOrder order = Mockito.inOrder(root);
        order.verify(root).addUniqueCommand(any(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(InitializeLoggerCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(AddPropertiesCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(HandHeldUnitEnablerCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(LogOnCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(root).addUniqueCommand(any(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
    }

    private ExecutionContext newTestExecutionContext() {
        return newTestExecutionContext(Logger.getAnonymousLogger());
    }

    private ExecutionContext newTestExecutionContext(Logger logger) {
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
                        comPort,
                        true,
                        this.serviceProvider);
        executionContext.setLogger(logger);
        return executionContext;
    }

}
