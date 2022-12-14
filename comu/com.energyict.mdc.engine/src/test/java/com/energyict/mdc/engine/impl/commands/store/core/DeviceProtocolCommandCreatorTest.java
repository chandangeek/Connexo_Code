/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
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
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionConnectionSteps;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.TypedProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolCommandCreatorTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private CommandRoot.ServiceProvider commandRootServiceProvider;
    @Mock
    private ExecutionContext.ServiceProvider executionContextServiceProvider;
    @Mock
    private IssueService issueService;
    @Mock
    private DeviceService deviceService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionTaskService connectionTaskService;
    @Mock
    private GroupedDeviceCommand clonedGroupedDeviceCommand;
    @Mock
    private ComTaskExecutionComCommandImpl comTaskRoot;
    @Mock
    private ComCommand comCommand;
    @Mock
    private ProtocolTask protocolTask;
    @Mock
    private ComTaskExecution scheduledComTask;

    private List<ProtocolTask> protocolTasks;
    private final Clock clock = Clock.systemDefaultZone();

    private ComTaskExecutionConnectionSteps createSingleDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(ComTaskExecutionConnectionSteps.FIRST_OF_CONNECTION_SERIES, ComTaskExecutionConnectionSteps.LAST_OF_CONNECTION_SERIES);
    }

    private ComTaskExecutionConnectionSteps createMiddleDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(0);
    }

    private ComTaskExecutionConnectionSteps createLastDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(ComTaskExecutionConnectionSteps.LAST_OF_CONNECTION_SERIES);
    }

    @Before
    public void initBefore() {
        when(this.executionContextServiceProvider.clock()).thenReturn(clock);
        when(this.executionContextServiceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.executionContextServiceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.commandRootServiceProvider.clock()).thenReturn(clock);
        when(this.commandRootServiceProvider.deviceService()).thenReturn(this.deviceService);
        when(clonedGroupedDeviceCommand.getComTaskRoot(scheduledComTask)).thenReturn(comTaskRoot);
        Map<ComCommandType, ComCommand> comCommands = new HashMap<>();
        comCommands.put(ComCommandTypes.BASIC_CHECK_COMMAND, comCommand);
        when(comTaskRoot.getCommands()).thenReturn(comCommands);
        ProtocolTask protocolTask = mock(ProtocolTask.class);
        protocolTasks = new ArrayList<>();
        protocolTasks.add(protocolTask);
    }

    @Test
    public void testCommandCreationOrder() {
        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getAllProperties()).thenReturn(TypedProperties.empty());
        CommandRootImpl commandRoot = new CommandRootImpl(this.newTestExecutionContext(), commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = spy(new GroupedDeviceCommand(commandRoot, device, deviceProtocol, null));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        doReturn(clonedGroupedDeviceCommand).when(groupedDeviceCommand).clone();
        when(scheduledComTask.getComTask()).thenReturn(comTask);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createSingleDeviceComTaskExecutionSteps();

        DeviceProtocolCommandCreator commandCreator = new DeviceProtocolCommandCreator();
        commandCreator.createCommands(
                groupedDeviceCommand,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                protocolTasks,
                null, comTaskExecutionConnectionStep, scheduledComTask, issueService);

        InOrder order = Mockito.inOrder(groupedDeviceCommand);
        order.verify(groupedDeviceCommand).clone();
        order.verify(groupedDeviceCommand).addCommand(isA(AddPropertiesCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(LogOnCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
    }

    @Test
    public void testMiddleStateCreationOrder() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRootImpl commandRoot = new CommandRootImpl(this.newTestExecutionContext(), commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = spy(new GroupedDeviceCommand(commandRoot, device, deviceProtocol, null));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        doReturn(clonedGroupedDeviceCommand).when(groupedDeviceCommand).clone();
        when(scheduledComTask.getComTask()).thenReturn(comTask);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createMiddleDeviceComTaskExecutionSteps();

        DeviceProtocolCommandCreator commandCreator = new DeviceProtocolCommandCreator();
        commandCreator.createCommands(
                groupedDeviceCommand,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                protocolTasks,
                null, comTaskExecutionConnectionStep, scheduledComTask, issueService);

        verify(groupedDeviceCommand, times(1)).clone();
        // none of the 'connection' related commands should be called ...
        verify(groupedDeviceCommand, times(0)).addCommand(isA(LogOffCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(LogOnCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(AddPropertiesCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(DaisyChainedLogOnCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(DaisyChainedLogOffCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
    }

    @Test
    public void testLastStateCreationOrderWithNoCommandsButLogonInPreviousStepExpectLogoff() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRootImpl commandRoot = new CommandRootImpl(this.newTestExecutionContext(), commandRootServiceProvider);
        GroupedDeviceCommand realGroupedDeviceCommand = new GroupedDeviceCommand(commandRoot, device, deviceProtocol, null);
        // Simulate Logon in previous step
        CommandFactory.createLogOnCommand(realGroupedDeviceCommand, scheduledComTask);
        GroupedDeviceCommand groupedDeviceCommand = spy(realGroupedDeviceCommand);
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        doReturn(clonedGroupedDeviceCommand).when(groupedDeviceCommand).clone();
        Map<ComCommandType, ComCommand> comCommands = new HashMap<>();
        when(comTaskRoot.getCommands()).thenReturn(comCommands);

        when(scheduledComTask.getComTask()).thenReturn(comTask);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createLastDeviceComTaskExecutionSteps();

        DeviceProtocolCommandCreator commandCreator = new DeviceProtocolCommandCreator();
        commandCreator.createCommands(
                groupedDeviceCommand,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                protocolTasks,
                null, comTaskExecutionConnectionStep,
                scheduledComTask,
                issueService);

        InOrder order = Mockito.inOrder(groupedDeviceCommand);
        order.verify(groupedDeviceCommand).clone();
        order.verify(groupedDeviceCommand).addCommand(isA(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));

        // the normal LogOn should never be called
        verify(groupedDeviceCommand, times(0)).addCommand(isA(LogOnCommand.class), any(ComTaskExecution.class));
    }

    @Test
    public void testLastStateCreationOrderWithNoCommandsButNoLogonInPreviousStepExpectNoLogoff() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRootImpl commandRoot = new CommandRootImpl(this.newTestExecutionContext(), commandRootServiceProvider);
        GroupedDeviceCommand realGroupedDeviceCommand = new GroupedDeviceCommand(commandRoot, device, deviceProtocol, null);
        GroupedDeviceCommand groupedDeviceCommand = spy(realGroupedDeviceCommand);
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        doReturn(clonedGroupedDeviceCommand).when(groupedDeviceCommand).clone();
        Map<ComCommandType, ComCommand> comCommands = new HashMap<>();
        when(comTaskRoot.getCommands()).thenReturn(comCommands);

        when(scheduledComTask.getComTask()).thenReturn(comTask);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createLastDeviceComTaskExecutionSteps();

        DeviceProtocolCommandCreator commandCreator = new DeviceProtocolCommandCreator();
        commandCreator.createCommands(
                groupedDeviceCommand,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                protocolTasks,
                null, comTaskExecutionConnectionStep,
                scheduledComTask,
                issueService);

        InOrder order = Mockito.inOrder(groupedDeviceCommand);
        order.verify(groupedDeviceCommand).clone();
        verify(groupedDeviceCommand, times(0)).addCommand(isA(LogOffCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        verify(groupedDeviceCommand, times(0)).addCommand(isA(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));

        // the normal LogOn should never be called
        verify(groupedDeviceCommand, times(0)).addCommand(isA(LogOnCommand.class), any(ComTaskExecution.class));
    }

    @Test
    public void testLastStateCreationOrder() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRootImpl commandRoot = new CommandRootImpl(this.newTestExecutionContext(), commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = spy(new GroupedDeviceCommand(commandRoot, device, deviceProtocol, null));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        doReturn(clonedGroupedDeviceCommand).when(groupedDeviceCommand).clone();
        when(scheduledComTask.getComTask()).thenReturn(comTask);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createLastDeviceComTaskExecutionSteps();

        DeviceProtocolCommandCreator commandCreator = new DeviceProtocolCommandCreator();
        commandCreator.createCommands(
                groupedDeviceCommand,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                protocolTasks,
                null, comTaskExecutionConnectionStep,
                scheduledComTask,
                issueService);

        InOrder order = Mockito.inOrder(groupedDeviceCommand);
        order.verify(groupedDeviceCommand).clone();
        order.verify(groupedDeviceCommand).addCommand(isA(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(isA(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));

        // the normal LogOn should never be called
        verify(groupedDeviceCommand, times(0)).addCommand(isA(LogOnCommand.class), any(ComTaskExecution.class));
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
                        executionContextServiceProvider);
        executionContext.setLogger(logger);
        return executionContext;
    }
}