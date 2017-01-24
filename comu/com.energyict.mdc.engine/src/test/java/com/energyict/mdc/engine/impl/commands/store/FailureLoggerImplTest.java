package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ExecutionLoggerImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-18 (10:43)
 */
@RunWith(MockitoJUnitRunner.class)
public class FailureLoggerImplTest {

    private static final long COM_TASK_EXECUTION_ID = 97;

    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskExecution comTaskExecution;

    @Before
    public void initializeMocks () {
        when(this.comTask.getName()).thenReturn(FailureLoggerImplTest.class.getSimpleName());
        when(this.comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(this.comTaskExecution.getComTask()).thenReturn(this.comTask);
    }

    @Test(expected = CodingException.class)
    public void testLogUnexpectedWithMissingComTaskSession () {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        ExecutionLoggerImpl failureLogger = mock(ExecutionLoggerImpl.class);
        doCallRealMethod().when(failureLogger).logUnexpected(any(Throwable.class), eq(this.comTaskExecution));
        when(failureLogger.getComSessionBuilder()).thenReturn(comSessionBuilder);
        when(comSessionBuilder.findFor(comTaskExecution)).thenReturn(Optional.empty());

        // Business method
        failureLogger.logUnexpected(new Exception("For testing purposes only"), this.comTaskExecution);

        // Asserts: CodingException expected
    }

    @Test
    public void testLogUnexpectedAddsJournalEntryToComSessionShadow () {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        ComTaskExecutionSessionBuilder taskExecutionSessionBuilder = mock(ComTaskExecutionSessionBuilder.class);
        when(comSessionBuilder.findFor(comTaskExecution)).thenReturn(Optional.of(taskExecutionSessionBuilder));
        ScheduledConnectionTask scheduledConnectionTask = mock(ScheduledConnectionTask.class);

        ComSession.SuccessIndicator successIndicator = ComSession.SuccessIndicator.Success;
        Clock clock = Clock.systemDefaultZone();
        ExecutionLoggerImpl failureLogger = new CreateOutboundComSession(Instant.now(), ComServer.LogLevel.DEBUG, scheduledConnectionTask, comSessionBuilder, successIndicator, clock);

        // Business method
        failureLogger.logUnexpected(new Exception("For testing purposes only"), this.comTaskExecution);

        // Asserts
        verify(taskExecutionSessionBuilder).addComCommandJournalEntry(any(Instant.class), eq(CompletionCode.UnexpectedError), anyString(), anyString());
    }

}