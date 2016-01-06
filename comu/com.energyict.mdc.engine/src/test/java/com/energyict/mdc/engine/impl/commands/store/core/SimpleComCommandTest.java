package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SimpleComCommand} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-10-02 (11:58)
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleComCommandTest {

    @Mock
    private CommandRoot commandRoot;

    @Test
    public void testGetIssuesWhenNoneExist () {
        SimpleComCommandForTestingPurposes command = this.newTestCommand();

        // Business method
        List<Issue> issues = command.getIssues();

        // Asserts
        assertThat(issues).isEmpty();
    }

    @Test
    public void testGetIssuess () {
        SimpleComCommandForTestingPurposes command =
                this.newTestCommand(
                        this.mockProblem("testGetIssuess-Problem1"),
                        this.mockProblem("testGetIssuess-Problem2"),
                        this.mockWarning("testGetIssuess-Warning"));

        // Business method
        List<Issue> issues = command.getIssues();
        List<Problem> problems = command.getProblems();
        List<Warning> warnings = command.getWarnings();

        // Asserts
        assertThat(issues).hasSize(3);
        assertThat(problems).hasSize(2);
        assertThat(warnings).hasSize(1);
    }

    @Test
    public void testGetIssuesFromCollectedData () {
        SimpleComCommandForTestingPurposes command =
                this.newTestCommand(
                        this.mockCollectedDataWithWarning("testGetIssuesFromCollectedData-Warning1"),
                        this.mockCollectedDataWithWarning("testGetIssuesFromCollectedData-Warning2"),
                        this.mockCollectedDataWithProblem("testGetIssuesFromCollectedData-Problem"));

        // Business method
        List<Issue> issues = command.getIssues();
        List<Problem> problems = command.getProblems();
        List<Warning> warnings = command.getWarnings();

        // Asserts
        assertThat(issues).hasSize(3);
        assertThat(problems).hasSize(1);
        assertThat(warnings).hasSize(2);
    }

    @Test
    public void testGetIssuesFromCollectedDataAndOthers () {
        SimpleComCommandForTestingPurposes command =
                this.newTestCommand(
                        this.mockCollectedDataWithWarning("testGetIssuesFromCollectedDataAndOthers-Collected-Warning1"),
                        this.mockCollectedDataWithWarning("testGetIssuesFromCollectedDataAndOthers-Collected-Warning2"),
                        this.mockCollectedDataWithProblem("testGetIssuesFromCollectedDataAndOthers-Collected-Problem"));
        command.addIssue(this.mockProblem("testGetIssuesFromCollectedDataAndOthers-Problem"));
        command.addIssue(this.mockWarning("testGetIssuesFromCollectedDataAndOthers-Warning"));

        // Business method
        List<Issue> issues = command.getIssues();
        List<Problem> problems = command.getProblems();
        List<Warning> warnings = command.getWarnings();

        // Asserts
        assertThat(issues).hasSize(5);
        assertThat(problems).hasSize(2);
        assertThat(warnings).hasSize(3);
    }

    private SimpleComCommandForTestingPurposes newTestCommand () {
        return new SimpleComCommandForTestingPurposes(this.commandRoot);
    }

    private SimpleComCommandForTestingPurposes newTestCommand (Issue... issues) {
        SimpleComCommandForTestingPurposes comCommand = newTestCommand();
        for (Issue issue : issues) {
            comCommand.addIssue(issue);
        }
        return comCommand;
    }

    private SimpleComCommandForTestingPurposes newTestCommand (CollectedData... collectedDatas) {
        SimpleComCommandForTestingPurposes comCommand = newTestCommand();
        for (CollectedData collectedData : collectedDatas) {
            comCommand.addCollectedDataItem(collectedData);
        }
        return comCommand;
    }

    private Warning mockWarning (String description) {
        Warning warning = mock(Warning.class);
        when(warning.isWarning()).thenReturn(true);
        when(warning.isProblem()).thenReturn(false);
        when(warning.getDescription()).thenReturn(description);
        return warning;
    }

    private Problem mockProblem (String description) {
        Problem problem = mock(Problem.class);
        when(problem.isWarning()).thenReturn(false);
        when(problem.isProblem()).thenReturn(true);
        when(problem.getDescription()).thenReturn(description);
        return problem;
    }

    private CollectedData mockCollectedDataWithWarning (String description) {
        CollectedData collectedData = mock(CollectedData.class);
        Issue warning = this.mockWarning(description);
        when(collectedData.getIssues()).thenReturn(Collections.singletonList(warning));
        return collectedData;
    }

    private CollectedData mockCollectedDataWithProblem (String description) {
        CollectedData collectedData = mock(CollectedData.class);
        Issue problem = this.mockProblem(description);
        when(collectedData.getIssues()).thenReturn(Collections.singletonList(problem));
        return collectedData;
    }

    private class SimpleComCommandForTestingPurposes extends SimpleComCommand {

        private SimpleComCommandForTestingPurposes (CommandRoot commandRoot) {
            super(commandRoot);
        }

        @Override
        public void doExecute (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
            // Remember: for testing purposes only
        }

        @Override
        public ComCommandType getCommandType () {
            return ComCommandTypes.UNKNOWN;
        }

        @Override
        public String getDescriptionTitle() {
            return "For testing purposes only";
        }
    }
}