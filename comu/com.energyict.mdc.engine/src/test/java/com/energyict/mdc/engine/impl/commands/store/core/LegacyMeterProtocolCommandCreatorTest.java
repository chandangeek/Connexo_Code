/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
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
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import java.time.Clock;
import java.util.Collections;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private DeviceProtocol deviceProtocol;
    @Mock
    private IssueService issueService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionTaskService connectionTaskService;
    @Mock
    private ExecutionContext.ServiceProvider serviceProvider;
    @Mock
    private CommandRoot.ServiceProvider commandRootServiceProvider;

    private ComTaskExecutionConnectionSteps createSingleDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(ComTaskExecutionConnectionSteps.FIRST_OF_CONNECTION_SERIES, ComTaskExecutionConnectionSteps.LAST_OF_CONNECTION_SERIES);
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
        when(device.getAllProperties()).thenReturn(TypedProperties.empty());
        CommandRootImpl commandRoot = new CommandRootImpl(this.newTestExecutionContext(), commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = spy(new GroupedDeviceCommand(commandRoot, device, deviceProtocol, null));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTask()).thenReturn(comTask);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createSingleDeviceComTaskExecutionSteps();

        LegacyMeterProtocolCommandCreator commandCreator = new LegacyMeterProtocolCommandCreator();
        commandCreator.createCommands(
                groupedDeviceCommand,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                Collections.<ProtocolTask>emptyList(),
                null, comTaskExecutionConnectionStep, null, issueService);

        InOrder order = Mockito.inOrder(groupedDeviceCommand);
        order.verify(groupedDeviceCommand).addCommand(any(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(InitializeLoggerCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(AddPropertiesCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(LogOnCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
    }

    @Test
    public void testCommandCreationOrderWithOptical() {
        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getAllProperties()).thenReturn(TypedProperties.empty());
        CommandRootImpl commandRoot = new CommandRootImpl(this.newTestExecutionContext(), commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = spy(new GroupedDeviceCommand(commandRoot, device, deviceProtocol, null));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTask()).thenReturn(comTask);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep = createSingleDeviceComTaskExecutionSteps();

        LegacyMeterProtocolCommandCreator commandCreator = new LegacyMeterProtocolCommandCreator();
        commandCreator.createCommands(
                groupedDeviceCommand,
                TypedProperties.empty(),
                ComChannelPlaceHolder.forKnownComChannel(comChannel),
                Collections.<ProtocolTask>emptyList(),
                null, comTaskExecutionConnectionStep, null, issueService);

        InOrder order = Mockito.inOrder(groupedDeviceCommand);
        order.verify(groupedDeviceCommand).addCommand(any(DeviceProtocolSetCacheCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(InitializeLoggerCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(AddPropertiesCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(DeviceProtocolInitializeCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(HandHeldUnitEnablerCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(LogOnCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(LogOffCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(DeviceProtocolTerminateCommand.class), any(ComTaskExecution.class));
        order.verify(groupedDeviceCommand).addCommand(any(DeviceProtocolUpdateCacheCommand.class), any(ComTaskExecution.class));
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
