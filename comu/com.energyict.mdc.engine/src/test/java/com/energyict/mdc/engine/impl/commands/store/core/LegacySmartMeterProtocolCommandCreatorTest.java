package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.comserver.commands.access.LogOffCommand;
import com.energyict.comserver.commands.access.LogOnCommand;
import com.energyict.comserver.commands.common.AddPropertiesCommand;
import com.energyict.comserver.commands.common.DeviceProtocolInitializeCommand;
import com.energyict.comserver.commands.common.DeviceProtocolSetCacheCommand;
import com.energyict.comserver.commands.common.DeviceProtocolTerminateCommand;
import com.energyict.comserver.commands.common.DeviceProtocolUpdateCacheCommand;
import com.energyict.comserver.commands.legacy.HandHeldUnitEnablerCommand;
import com.energyict.comserver.commands.legacy.InitializeLoggerCommand;
import com.energyict.comserver.core.ComTaskExecutionConnectionSteps;
import com.energyict.comserver.core.JobExecution;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.protocol.ComChannelPlaceHolder;
import com.energyict.mdc.protocol.ComPortRelatedComChannel;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.device.config.ProtocolTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ConnectionTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.protocols.mdc.channels.serial.SerialComChannel;
import com.energyict.protocols.mdc.channels.serial.ServerSerialPort;
import java.util.Collections;
import java.util.logging.Logger;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 19/12/12 - 15:19
 */
public class LegacySmartMeterProtocolCommandCreatorTest {

    private static final int COMPORT_POOL_ID = 1;
    private static final int COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final int CONNECTION_TASK_ID = COMPORT_ID + 1;

    private ComTaskExecutionConnectionSteps createSingleDeviceComTaskExecutionSteps() {
        return new ComTaskExecutionConnectionSteps(ComTaskExecutionConnectionSteps.FIRST_OF_CONNECTION_SERIES, ComTaskExecutionConnectionSteps.LAST_OF_CONNECTION_SERIES);
    }

    @Test
    public void testCommandCreationOrder() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot root = spy(new CommandRootImpl(device, this.newTestExecutionContext()));
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTask()).thenReturn(comTask);
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
        CommandRoot root = spy(new CommandRootImpl(device, this.newTestExecutionContext()));

        SerialComChannel comChannel = mock(SerialComChannel.class);
        ServerSerialPort serverSerialPort = mock(ServerSerialPort.class);
        when(comChannel.getSerialPort()).thenReturn(serverSerialPort);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution scheduledComTask = mock(ComTaskExecution.class);
        when(scheduledComTask.getComTask()).thenReturn(comTask);
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

    private JobExecution.ExecutionContext newTestExecutionContext () {
        return newTestExecutionContext(Logger.getAnonymousLogger());
    }

    private JobExecution.ExecutionContext newTestExecutionContext (Logger logger) {
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
        JobExecution.ExecutionContext executionContext =
                new JobExecution.ExecutionContext(
                        mock(JobExecution.class),
                        connectionTask,
                        comPort, issueService);
        executionContext.setLogger(logger);
        return executionContext;
    }

}