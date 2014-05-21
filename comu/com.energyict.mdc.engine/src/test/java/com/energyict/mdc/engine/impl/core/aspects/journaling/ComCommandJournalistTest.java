package com.energyict.mdc.engine.impl.core.aspects.journaling;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.tasks.history.CompletionCode;
import org.fest.assertions.api.Assertions;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ComCommandJournalist} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-15 (17:17)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComCommandJournalistTest {

    private ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder;
    private ComCommandJournalist journalist;
    private Clock clock = new ProgrammableClock().frozenAt(new Date(-1613851200000L)); // GMT + 1 : what happened then?

    @Before
    public void initializeJournalist () {
        this.journalist = new ComCommandJournalist(this.comTaskExecutionSessionBuilder, clock);
    }

    @Test
    public void testCommandWithoutIssuesAtLowerPriorityLogLevel () {
        String expectedCommandDescription = "testCommandWithoutIssuesAtLowerPriorityLogLevel";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.DEBUG);
        CompletionCode expectedCompletionCode = CompletionCode.ConnectionError;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toString()).thenReturn(expectedCommandDescription);

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);



        // Asserts
        verify(comTaskExecutionSessionBuilder, never()).addComCommandJournalEntry(any(Date.class), any(CompletionCode.class), anyString(), anyString());
        verify(comTaskExecutionSessionBuilder, never()).addComTaskExecutionMessageJournalEntry(any(Date.class), anyString(), anyString());
    }

    @Test
    public void testCommandWithoutIssuesAtEqualPriorityLogLevel () {
        String expectedCommandDescription = "testCommandWithoutIssuesAtEqualPriorityLogLevel";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.INFO);
        CompletionCode expectedCompletionCode = CompletionCode.ConnectionError;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toJournalMessageDescription(any(LogLevel.class))).thenReturn(expectedCommandDescription);

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.INFO);

        // Asserts
        verify(comTaskExecutionSessionBuilder).addComCommandJournalEntry(clock.now(), expectedCompletionCode, null, expectedCommandDescription);
    }

    @Test
    public void testCommandWithoutIssuesAtHigherPriorityLogLevel () {
        String expectedCommandDescription = "testCommandWithoutIssuesAtHigherPriorityLogLevel";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.INFO);
        CompletionCode expectedCompletionCode = CompletionCode.ConnectionError;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toJournalMessageDescription(any(LogLevel.class))).thenReturn(expectedCommandDescription);

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.DEBUG);

        // Asserts
        verify(comTaskExecutionSessionBuilder).addComCommandJournalEntry(clock.now(), expectedCompletionCode, null, expectedCommandDescription);
    }

    @Test
    public void testCommandWithoutIssues () {
        String expectedCommandDescription = "testCommandWithoutIssues";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.ERROR);
        CompletionCode expectedCompletionCode = CompletionCode.ConfigurationWarning;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toString()).thenReturn(expectedCommandDescription);
        when(comCommand.getIssues()).thenReturn(new ArrayList<Issue<?>>(0));
        when(comCommand.getProblems()).thenReturn(new ArrayList<Problem<?>>(0));
        when(comCommand.getWarnings()).thenReturn(new ArrayList<Warning<?>>(0));

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);

        // Asserts
        verify(comTaskExecutionSessionBuilder).addComCommandJournalEntry(clock.now(), expectedCompletionCode, null, expectedCommandDescription);
    }

    @Test
    public void testCommandWithOnlyWarnings () {
        String expectedCommandDescription = "testCommandWithOnlyWarnings";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.ERROR);
        CompletionCode expectedCompletionCode = CompletionCode.ConfigurationWarning;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toString()).thenReturn(expectedCommandDescription);
        Warning<?> warning1 = this.mockWarning("First warning");
        Warning<?> warning2 = this.mockWarning("Second warning");
        when(comCommand.getIssues()).thenReturn(Arrays.<Issue<?>>asList(warning1, warning2));
        when(comCommand.getWarnings()).thenReturn(Arrays.<Warning<?>>asList(warning1, warning2));
        when(comCommand.getProblems()).thenReturn(new ArrayList<Problem<?>>(0));

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);

        // Asserts
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(comTaskExecutionSessionBuilder).addComCommandJournalEntry(eq(clock.now()), eq(expectedCompletionCode), stringCaptor.capture(), eq(expectedCommandDescription));
        String errorDescription = stringCaptor.getValue();
        Assertions.assertThat(errorDescription).startsWith("Execution completed with 2 warning(s) and 0 problem(s)");
        Assertions.assertThat(errorDescription).contains("01. First warning");
        Assertions.assertThat(errorDescription).contains("02. Second warning");
    }

    @Test
    public void testCommandWithOnlyProblems () {
        String expectedCommandDescription = "testCommandWithOnlyProblems";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.ERROR);
        CompletionCode expectedCompletionCode = CompletionCode.ConfigurationError;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toString()).thenReturn(expectedCommandDescription);
        Problem<?> problem1 = this.mockProblem("First problem");
        Problem<?> problem2 = this.mockProblem("Second problem");
        when(comCommand.getIssues()).thenReturn(Arrays.<Issue<?>>asList(problem1, problem2));
        when(comCommand.getWarnings()).thenReturn(new ArrayList<Warning<?>>(0));
        when(comCommand.getProblems()).thenReturn(Arrays.<Problem<?>>asList(problem1, problem2));

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);

        // Asserts
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(comTaskExecutionSessionBuilder).addComCommandJournalEntry(eq(clock.now()), eq(expectedCompletionCode), stringCaptor.capture(), eq(expectedCommandDescription));
        String errorDescription = stringCaptor.getValue();
        Assertions.assertThat(errorDescription).startsWith("Execution completed with 0 warning(s) and 2 problem(s)");
        Assertions.assertThat(errorDescription).contains("01. First warning");
        Assertions.assertThat(errorDescription).contains("02. Second warning");
    }

    @Test
    public void testCommandWithWarningsAndProblems () {
        String expectedCommandDescription = "testCommandWithWarningsAndProblems";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.ERROR);
        CompletionCode expectedCompletionCode = CompletionCode.ConfigurationError;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toString()).thenReturn(expectedCommandDescription);
        Problem<?> problem = this.mockProblem("Problem");
        Warning<?> warning = this.mockWarning("Warning");
        when(comCommand.getIssues()).thenReturn(Arrays.<Issue<?>>asList(problem, warning));
        when(comCommand.getWarnings()).thenReturn(Arrays.<Warning<?>>asList(warning));
        when(comCommand.getProblems()).thenReturn(Arrays.<Problem<?>>asList(problem));

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);

        // Asserts
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(comTaskExecutionSessionBuilder).addComCommandJournalEntry(eq(clock.now()), eq(expectedCompletionCode), stringCaptor.capture(), eq(expectedCommandDescription));
        String errorDescription = stringCaptor.getValue();
        Assertions.assertThat(errorDescription).startsWith("Execution completed with 1 warning(s) and 1 problem(s)");
        Assertions.assertThat(errorDescription).contains("01. First warning");
        Assertions.assertThat(errorDescription).contains("02. Second warning");
    }

    private Warning<?> mockWarning (String description) {
        Warning<?> warning = mock(Warning.class);
        when(warning.isWarning()).thenReturn(true);
        when(warning.isProblem()).thenReturn(false);
        when(warning.getDescription()).thenReturn(description);
        when(warning.getTimestamp()).thenReturn(clock.now());
        return warning;
    }

    private Problem<?> mockProblem (String description) {
        Problem<?> problem = mock(Problem.class);
        when(problem.isWarning()).thenReturn(false);
        when(problem.isProblem()).thenReturn(true);
        when(problem.getDescription()).thenReturn(description);
        when(problem.getTimestamp()).thenReturn(clock.now());
        return problem;
    }

}