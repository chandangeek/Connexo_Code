/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;
import com.energyict.mdc.common.tasks.ClockTask;
import com.energyict.mdc.common.tasks.ClockTaskType;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
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
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.issue.Warning;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.CommunicationInterruptedException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Locale;
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
    protected Clock clock = Clock.fixed(LocalDateTime.of(2014, 5, 20, 16, 16, 17, 222).atZone(ZoneOffset.systemDefault()).toInstant(), ZoneId.systemDefault());
    @Mock
    protected EventPublisher eventPublisher;
    @Mock
    protected NlsService nlsService;
    @Mock
    protected Thesaurus thesaurusCES;
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
        doAnswer(invocationOnMock -> Stream.of(invocationOnMock.getArguments()).filter(o -> o instanceof ConnectionTask).findAny().orElse(null)).when(comServerDAO)
                .executionStarted(any(ConnectionTask.class), any(ComPort.class));
        doAnswer(invocationOnMock -> Stream.of(invocationOnMock.getArguments()).filter(o -> o instanceof ConnectionTask).findAny().orElse(null)).when(comServerDAO)
                .executionFailed(any(ConnectionTask.class));
        doAnswer(invocationOnMock -> Stream.of(invocationOnMock.getArguments()).filter(o -> o instanceof ConnectionTask).findAny().orElse(null)).when(comServerDAO)
                .executionCompleted(any(ConnectionTask.class));
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
        when(this.commandRootServiceProvider.nlsService()).thenReturn(this.nlsService);
        when(this.nlsService.getThesaurus("CES", Layer.DOMAIN)).thenReturn(this.thesaurusCES);

        when(thesaurusCES.getString(any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        when(thesaurusCES.getString(any(), any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        when(thesaurusCES.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((TranslationKey) invocation.getArguments()[0]));
        when(thesaurusCES.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((MessageSeed) invocation.getArguments()[0]));


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
        doThrow(new CommunicationException(MessageSeeds.LOGBOOK_ISSUE, new IOException("Hi, I'm an error"))).when(clockCommand).doExecute(any(DeviceProtocol.class), any(ExecutionContext.class));
        groupedDeviceCommand.addCommand(clockCommand, comTaskExecution);
    }

    protected void mockConnectionErrorFailureComCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.SETCLOCK);
        ClockCommandImpl clockCommand = spy(new ClockCommandImpl(groupedDeviceCommand, clockTask, comTaskExecution));
        doThrow(new ConnectionCommunicationException(com.energyict.mdc.engine.impl.commands.MessageSeeds.UNEXPECTED_IO_EXCEPTION, new IOException("For testing purposes only")))
                .when(clockCommand)
                .doExecute(any(DeviceProtocol.class), any(ExecutionContext.class));
        groupedDeviceCommand.addCommand(clockCommand, comTaskExecution);
    }

    protected void mockConnectionInterruptedFailureComCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.SETCLOCK);
        ClockCommandImpl clockCommand = spy(new ClockCommandImpl(groupedDeviceCommand, clockTask, comTaskExecution));
        doThrow(new CommunicationInterruptedException(com.energyict.mdc.engine.impl.commands.MessageSeeds.NOT_EXECUTED_DUE_TO_CONNECTION_INTERRUPTED, new IOException("Hi, I'm interrupting your connection"))).when(clockCommand)
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
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(nextExecutionSpecs.getNextTimestamp(any(Calendar.class)))
                .thenAnswer(invocation -> {
                    Calendar calendar = (Calendar) invocation.getArguments()[0];
                    calendar.add(Calendar.MINUTE, 5);
                    return calendar.getTime();
                });
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));
        ComTask comTask = mock(ComTask.class);
        when(comTaskExecution1.getComTask()).thenReturn(comTask);
        return comTaskExecution1;
    }

    class SimpleNlsMessageFormat implements NlsMessageFormat {

        private final String key;

        SimpleNlsMessageFormat(TranslationKey translationKey) {
            this.key = translationKey.getKey();
        }

        SimpleNlsMessageFormat(MessageSeed messageSeed) {
            this.key = messageSeed.getKey();
        }

        @Override
        public String format(Object... args) {
            return this.key;    // Don't format, just return the key
        }

        @Override
        public String format(Locale locale, Object... args) {
            return this.key;    // Don't format, just return the key
        }
    }
}
