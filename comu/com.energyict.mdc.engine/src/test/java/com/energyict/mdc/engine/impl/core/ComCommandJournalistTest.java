package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.issue.Warning;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComCommandJournalist} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-15 (17:17)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComCommandJournalistTest {

    @Mock
    private JournalEntryFactory journalEntryFactory;

    private ComCommandJournalist journalist;
    private Clock clock = Clock.fixed(Instant.ofEpochMilli(-1613851200000L), ZoneId.systemDefault()); // GMT + 1 : what happened then? I'll tell you: armistics of WOI, i.e. Nov 11th 1918

    @Before
    public void initializeJournalist () {
        this.journalist = new ComCommandJournalist(this.journalEntryFactory, clock);
    }

    @Test
    public void testCommandWithoutIssuesAtLowerPriorityLogLevel () {
        String expectedCommandDescription = "testCommandWithoutIssuesAtLowerPriorityLogLevel";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.DEBUG);
        CompletionCode expectedCompletionCode = CompletionCode.ConnectionError;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toJournalMessageDescription(any(LogLevel.class))).thenReturn(expectedCommandDescription);

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);

        // Asserts
        verify(this.journalEntryFactory, never()).createComCommandJournalEntry(any(Instant.class), any(CompletionCode.class), anyString(), anyString());
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
        verify(this.journalEntryFactory).createComCommandJournalEntry(clock.instant(), expectedCompletionCode, "", expectedCommandDescription);
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
        verify(this.journalEntryFactory).createComCommandJournalEntry(clock.instant(), expectedCompletionCode, "", expectedCommandDescription);
    }

    @Test
    public void testCommandWithoutIssues () {
        String expectedCommandDescription = "testCommandWithoutIssues";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.ERROR);
        CompletionCode expectedCompletionCode = CompletionCode.ConfigurationWarning;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toJournalMessageDescription(any(LogLevel.class))).thenReturn(expectedCommandDescription);
        when(comCommand.getIssues()).thenReturn(new ArrayList<>(0));
        when(comCommand.getProblems()).thenReturn(new ArrayList<>(0));
        when(comCommand.getWarnings()).thenReturn(new ArrayList<>(0));

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);

        // Asserts
        verify(this.journalEntryFactory).createComCommandJournalEntry(clock.instant(), expectedCompletionCode, "", expectedCommandDescription);
    }

    @Test
    public void testCommandWithOnlyWarnings () {
        String expectedCommandDescription = "testCommandWithOnlyWarnings";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.ERROR);
        CompletionCode expectedCompletionCode = CompletionCode.ConfigurationWarning;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toJournalMessageDescription(any(LogLevel.class))).thenReturn(expectedCommandDescription);
        Warning warning1 = this.mockWarning("First warning");
        Warning warning2 = this.mockWarning("Second warning");
        when(comCommand.getIssues()).thenReturn(Arrays.<Issue>asList(warning1, warning2));
        when(comCommand.getWarnings()).thenReturn(Arrays.asList(warning1, warning2));
        when(comCommand.getProblems()).thenReturn(new ArrayList<>(0));

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);

        // Asserts
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.journalEntryFactory).createComCommandJournalEntry(eq(clock.instant()), eq(expectedCompletionCode), stringCaptor.capture(), eq(expectedCommandDescription));
        String errorDescription = stringCaptor.getValue();
        assertThat(errorDescription).startsWith("Execution completed with 2 warning(s) and 0 problem(s)");
        assertThat(errorDescription).contains("01. First warning");
        assertThat(errorDescription).contains("02. Second warning");
    }

    @Test
    public void testCommandWithOnlyProblems () {
        String expectedCommandDescription = "testCommandWithOnlyProblems";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.ERROR);
        CompletionCode expectedCompletionCode = CompletionCode.ConfigurationError;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toJournalMessageDescription(any(LogLevel.class))).thenReturn(expectedCommandDescription);
        Problem problem1 = this.mockProblem("First problem");
        Problem problem2 = this.mockProblem("Second problem");
        when(comCommand.getIssues()).thenReturn(Arrays.<Issue>asList(problem1, problem2));
        when(comCommand.getWarnings()).thenReturn(new ArrayList<>(0));
        when(comCommand.getProblems()).thenReturn(Arrays.asList(problem1, problem2));

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);

        // Asserts
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.journalEntryFactory).createComCommandJournalEntry(eq(clock.instant()), eq(expectedCompletionCode), stringCaptor.capture(), eq(expectedCommandDescription));
        String errorDescription = stringCaptor.getValue();
        assertThat(errorDescription).startsWith("Execution completed with 0 warning(s) and 2 problem(s)");
        assertThat(errorDescription).contains("01. First problem");
        assertThat(errorDescription).contains("02. Second problem");
    }

    @Test
    public void testCommandWithWarningsAndProblems () {
        String expectedCommandDescription = "testCommandWithWarningsAndProblems";
        ComCommand comCommand = mock(ComCommand.class);
        when(comCommand.getJournalingLogLevel()).thenReturn(LogLevel.ERROR);
        CompletionCode expectedCompletionCode = CompletionCode.ConfigurationError;
        when(comCommand.getCompletionCode()).thenReturn(expectedCompletionCode);
        when(comCommand.toJournalMessageDescription(any(LogLevel.class))).thenReturn(expectedCommandDescription);
        Problem problem = this.mockProblem("Problem");
        Warning warning = this.mockWarning("Warning");
        when(comCommand.getIssues()).thenReturn(Arrays.asList(problem, warning));
        when(comCommand.getWarnings()).thenReturn(Arrays.asList(warning));
        when(comCommand.getProblems()).thenReturn(Arrays.asList(problem));

        // Business method
        this.journalist.executionCompleted(comCommand, LogLevel.ERROR);

        // Asserts
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.journalEntryFactory).createComCommandJournalEntry(eq(clock.instant()), eq(expectedCompletionCode), stringCaptor.capture(), eq(expectedCommandDescription));
        String errorDescription = stringCaptor.getValue();
        assertThat(errorDescription).startsWith("Execution completed with 1 warning(s) and 1 problem(s)");
        assertThat(errorDescription).contains("01. Problem");
        assertThat(errorDescription).contains("01. Warning");
    }

    private Warning mockWarning (String description) {
        Warning warning = mock(Warning.class);
        when(warning.isWarning()).thenReturn(true);
        when(warning.isProblem()).thenReturn(false);
        when(warning.getDescription()).thenReturn(description);
        when(warning.getTimestamp()).thenReturn(clock.instant());
        when(warning.getException()).thenReturn(Optional.empty());
        return warning;
    }

    private Problem mockProblem (String description) {
        Problem problem = mock(Problem.class);
        when(problem.isWarning()).thenReturn(false);
        when(problem.isProblem()).thenReturn(true);
        when(problem.getDescription()).thenReturn(description);
        when(problem.getTimestamp()).thenReturn(clock.instant());
        when(problem.getException()).thenReturn(Optional.empty());
        return problem;
    }

}