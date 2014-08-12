package com.energyict.mdc.engine;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.SetClockCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import com.elster.jupiter.util.time.ProgrammableClock;
import org.fest.assertions.data.MapEntry;

import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 29/01/13
 * Time: 15:10
 * Author: khe
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericDeviceProtocolTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;

    @Mock
    OfflineDevice offlineDevice;

    @Mock
    ReadRegistersCommand readRegistersCommand;

    @Mock
    SetClockCommand setClockCommand;

    private FakeServiceProvider serviceProvider;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DeviceDataService deviceDataService;

    @Before
    public void initMock() {
        when(readRegistersCommand.getCommandType()).thenReturn(ComCommandTypes.READ_REGISTERS_COMMAND);
        when(setClockCommand.getCommandType()).thenReturn(ComCommandTypes.SET_CLOCK_COMMAND);
    }

    @Test
    public void testOrganizeComCommands() throws Exception {
        MockGenericDeviceProtocol protocol = new MockGenericDeviceProtocol();
        protocol.init(offlineDevice, null);

        serviceProvider = new FakeServiceProvider();
        serviceProvider.setClock(new ProgrammableClock());
        serviceProvider.setDeviceDataService(deviceDataService);
        CommandRootImpl root = new CommandRootImpl(offlineDevice, newTestExecutionContext(this.serviceProvider), this.serviceProvider);
        root.addCommand(readRegistersCommand, null);
        root.addCommand(setClockCommand, null);

        CommandRoot result = protocol.organizeComCommands(root);
        assertThat(result.getCommands()).hasSize(root.getCommands().size() - 1);
        assertThat(result.getCommands()).contains(MapEntry.entry(setClockCommand.getCommandType(), setClockCommand));
        assertThat(result.getCommands()).doesNotContain(MapEntry.entry(readRegistersCommand.getCommandType(), readRegistersCommand));
        assertThat(root.getCommands()).contains(MapEntry.entry(setClockCommand.getCommandType(), setClockCommand));
        assertThat(root.getCommands()).contains(MapEntry.entry(readRegistersCommand.getCommandType(), readRegistersCommand));
    }

    private ExecutionContext newTestExecutionContext(ExecutionContext.ServiceProvider serviceProvider) {
        return newTestExecutionContext(Logger.getAnonymousLogger(), serviceProvider);
    }

    private ExecutionContext newTestExecutionContext(Logger logger, ExecutionContext.ServiceProvider serviceProvider) {
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
                        serviceProvider);
        executionContext.setLogger(logger);
        return executionContext;
    }

}