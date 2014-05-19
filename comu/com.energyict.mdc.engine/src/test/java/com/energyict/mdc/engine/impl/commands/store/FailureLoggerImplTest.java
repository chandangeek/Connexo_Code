package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.shadow.journal.ComSessionShadow;
import com.energyict.mdc.shadow.journal.ComTaskExecutionSessionShadow;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTaskExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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

    private static final int COM_TASK_EXECUTION_ID = 97;

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
        ComSessionShadow shadow = new ComSessionShadow();
        ExecutionLoggerImpl failureLogger = mock(ExecutionLoggerImpl.class);
        doCallRealMethod().when(failureLogger).logUnexpected(any(Throwable.class), eq(this.comTaskExecution));
        when(failureLogger.getComSessionBuilder()).thenReturn(shadow);

        // Business method
        failureLogger.logUnexpected(new Exception("For testing purposes only"), this.comTaskExecution);

        // Asserts: CodingException expected
    }

    @Test
    public void testLogUnexpectedAddsJournalEntryToComSessionShadow () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        ComTaskExecutionSessionShadow comTaskExecutionSessionShadow = new ComTaskExecutionSessionShadow();
        comTaskExecutionSessionShadow.setComTaskExecutionId(COM_TASK_EXECUTION_ID);
        comSessionShadow.addComTaskSession(comTaskExecutionSessionShadow);
        ExecutionLoggerImpl failureLogger = mock(ExecutionLoggerImpl.class);
        doCallRealMethod().when(failureLogger).logUnexpected(any(Throwable.class), eq(this.comTaskExecution));
        when(failureLogger.getComSessionBuilder()).thenReturn(comSessionShadow);

        // Business method
        failureLogger.logUnexpected(new Exception("For testing purposes only"), this.comTaskExecution);

        // Asserts
        verify(failureLogger).getComSessionBuilder();
        Assertions.assertThat(comTaskExecutionSessionShadow.getJournalEntryShadows()).isNotEmpty();
    }

}