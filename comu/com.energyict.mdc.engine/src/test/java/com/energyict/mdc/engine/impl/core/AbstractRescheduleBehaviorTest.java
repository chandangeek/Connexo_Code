package com.energyict.mdc.engine.impl.core;


import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.BasicComCommandBehavior;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ClockCommandImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.upl.io.ConnectionCommunicationException;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.issue.Warning;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import org.joda.time.DateTime;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 04.04.16
 * Time: 16:08
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractRescheduleBehaviorTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    protected static final long CONNECTION_TASK_ID = COMPORT_ID + 1;

    @Mock
    protected ExecutionContext.ServiceProvider serviceProvider;
    @Mock
    protected CommandRoot.ServiceProvider commandRootServiceProvider;
    @Mock
    protected JobExecution.ServiceProvider jobExecServiceProvider;
    @Mock
    protected IssueService issueService;
    @Mock
    protected ConnectionTaskService connectionTaskService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected ComSessionBuilder comSessionBuilder;
    protected Clock clock = Clock.fixed(new DateTime(2014, 5, 20, 16, 16, 17, 222).toDate().toInstant(), ZoneId.systemDefault());
    @Mock
    protected EventPublisher eventPublisher;
    @Mock
    protected ComServerDAO comServerDAO;

    @Mock
    protected ScheduledConnectionTask connectionTask;
    @Mock
    protected DeviceProtocol deviceProtocol;
    @Mock
    protected DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;
    @Mock
    protected OfflineDevice offlineDevice;
    @Mock
    protected ComTaskExecution comTaskExecution;
    @Mock
    protected ComTask comTask;
    @Mock
    private Device device;

    @Before
    public void setup() {
        doAnswer(invocationOnMock -> Stream.of(invocationOnMock.getArguments()).filter(o ->  o instanceof ConnectionTask).findAny().orElse(null)).when(comServerDAO).executionStarted(any(ConnectionTask.class), any(ComServer.class));
        doAnswer(invocationOnMock -> Stream.of(invocationOnMock.getArguments()).filter(o ->  o instanceof ConnectionTask).findAny().orElse(null)).when(comServerDAO).executionFailed(any(ConnectionTask.class));
        doAnswer(invocationOnMock -> Stream.of(invocationOnMock.getArguments()).filter(o ->  o instanceof ConnectionTask).findAny().orElse(null)).when(comServerDAO).executionCompleted(any(ConnectionTask.class));
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getDevice()).thenReturn(device);
        Optional<NextExecutionSpecs> nextExecutionSpecs = Optional.empty();
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(nextExecutionSpecs);


        Problem problem = mock(Problem.class);
        when(problem.isProblem()).thenReturn(true);
        when(problem.getTimestamp()).thenReturn(Instant.now());
        when(problem.getException()).thenReturn(Optional.empty());
        when(issueService.newProblem(any(Object.class), any(MessageSeed.class))).thenReturn(problem);
        when(issueService.newProblem(any(Object.class), any(MessageSeed.class), any(Object.class))).thenReturn(problem);
        when(issueService.newProblem(any(Object.class), any(MessageSeed.class), any(Object.class), any(Object.class))).thenReturn(problem);
        when(issueService.newProblem(any(Object.class), any(MessageSeed.class), any(Object.class), any(Object.class), any(Object.class))).thenReturn(problem);
        Warning warning = mock(Warning.class);
        when(warning.isWarning()).thenReturn(true);
        when(warning.getTimestamp()).thenReturn(Instant.now());
        when(warning.getException()).thenReturn(Optional.empty());
        when(issueService.newWarning(any(Object.class), any(MessageSeed.class))).thenReturn(warning);
        when(issueService.newWarning(any(Object.class), any(MessageSeed.class), any(Object.class))).thenReturn(warning);
        when(issueService.newWarning(any(Object.class), any(MessageSeed.class), any(Object.class), any(Object.class))).thenReturn(warning);
        when(issueService.newWarning(any(Object.class), any(MessageSeed.class), any(Object.class), any(Object.class), any(Object.class))).thenReturn(warning);

        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.issueService()).thenReturn(this.issueService);
        when(this.serviceProvider.clock()).thenReturn(clock);
        when(this.serviceProvider.eventPublisher()).thenReturn(this.eventPublisher);

        when(this.jobExecServiceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.jobExecServiceProvider.issueService()).thenReturn(this.issueService);
        when(this.jobExecServiceProvider.clock()).thenReturn(clock);
        when(this.jobExecServiceProvider.eventPublisher()).thenReturn(this.eventPublisher);

        when(this.commandRootServiceProvider.issueService()).thenReturn(this.issueService);
        when(this.commandRootServiceProvider.clock()).thenReturn(clock);

        when(this.connectionTaskService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Instant.class))).thenReturn(comSessionBuilder);
    }

    protected CommandRootImpl createMockedCommandRootWithCommands(ComCommand... comCommands) {
        CommandRootImpl commandRoot = new CommandRootImpl(newTestExecutionContext(), commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = commandRoot.getOrCreateGroupedDeviceCommand(offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet);
        for (ComCommand comCommand : comCommands) {
            groupedDeviceCommand.addCommand(comCommand, comTaskExecution);
        }
        return commandRoot;
    }

    protected CommandRootImpl createMockedCommandRootWithPairs(Pair<ComCommand, ComTaskExecution>... pairs) {
        CommandRootImpl commandRoot = new CommandRootImpl(newTestExecutionContext(), commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = commandRoot.getOrCreateGroupedDeviceCommand(offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet);
        for (Pair<ComCommand, ComTaskExecution> pair : pairs) {
            groupedDeviceCommand.addCommand(pair.getFirst(), pair.getLast());
        }
        return commandRoot;
    }

    protected SimpleComCommand mockSuccessfulComCommand() {
        SimpleComCommand successfulComCommand = mock(SimpleComCommand.class);
        when(successfulComCommand.getCommandType()).thenReturn(ComCommandTypes.CLOCK_COMMAND);
        when(successfulComCommand.getCompletionCode()).thenReturn(CompletionCode.Ok);
        when(successfulComCommand.getExecutionState()).thenReturn(BasicComCommandBehavior.ExecutionState.SUCCESSFULLY_EXECUTED);
        return successfulComCommand;
    }

    protected void mockFailureComCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.SETCLOCK);
        ClockCommandImpl clockCommand = spy(new ClockCommandImpl(groupedDeviceCommand, clockTask, comTaskExecution));
        doThrow(new CommunicationException(MessageSeeds.UNEXPECTED_PROTOCOL_ERROR, new IOException("Hi, I'm an error"))).when(clockCommand).doExecute(any(DeviceProtocol.class), any(ExecutionContext.class));
        groupedDeviceCommand.addCommand(clockCommand, comTaskExecution);
    }

    protected void mockConnectionErrorFailureComCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.SETCLOCK);
        ClockCommandImpl clockCommand = spy(new ClockCommandImpl(groupedDeviceCommand, clockTask, comTaskExecution));
        doThrow(new ConnectionCommunicationException(1)).when(clockCommand)
                .doExecute(any(DeviceProtocol.class), any(ExecutionContext.class));
        groupedDeviceCommand.addCommand(clockCommand, comTaskExecution);
    }

    protected SimpleComCommand mockNotExecutedComCommand() {
        SimpleComCommand notExecutedComCommand = mock(SimpleComCommand.class);
        when(notExecutedComCommand.getCommandType()).thenReturn(ComCommandTypes.CLOCK_COMMAND);
        when(notExecutedComCommand.getCompletionCode()).thenReturn(CompletionCode.Ok);
        when(notExecutedComCommand.getExecutionState()).thenReturn(BasicComCommandBehavior.ExecutionState.NOT_EXECUTED);
        return notExecutedComCommand;
    }

    private ExecutionContext newTestExecutionContext() {
        ExecutionContext executionContext = newTestExecutionContext(Logger.getAnonymousLogger());
        executionContext.connecting = new StopWatch();
        executionContext.executing = new StopWatch(false);  // Do not auto start but start it manually as soon as execution starts.

        return executionContext;
    }

    private ExecutionContext newTestExecutionContext(Logger logger) {
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(COMPORT_POOL_ID);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getComServer()).thenReturn(comServer);
        when(connectionTask.getId()).thenReturn(RescheduleBehaviorForAsapTest.CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        DeviceCommandExecutor deviceCommandExecutor = mock(DeviceCommandExecutor.class);
        ScheduledComTaskExecutionGroup comTaskExecutionGroup = new ScheduledComTaskExecutionGroup(comPort, comServerDAO, deviceCommandExecutor, connectionTask, jobExecServiceProvider);
        comTaskExecutionGroup.createExecutionContext();
        ExecutionContext executionContext = comTaskExecutionGroup.getExecutionContext();
        executionContext.setLogger(logger);
        return executionContext;
    }

    protected ComTaskExecution mockNewComTaskExecution() {
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getNextExecutionSpecs()).thenReturn(Optional.empty());
        ComTask comTask = mock(ComTask.class);
        when(comTaskExecution1.getComTask()).thenReturn(comTask);
        return comTaskExecution1;
    }

}
