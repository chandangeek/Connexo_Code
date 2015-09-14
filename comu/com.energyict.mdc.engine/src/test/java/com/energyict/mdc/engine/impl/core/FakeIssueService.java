package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.issues.IssueCollector;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Instant;

import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides a fake implementation for the {@link IssueService}
 * that returns all mocked objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-23 (16:29)
 */
public class FakeIssueService implements IssueService {

    @Override
    public IssueCollector newIssueCollector() {
        IssueCollector issueCollector = mock(IssueCollector.class);
        ArgumentCaptor<Object> sourceCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
        when(issueCollector.addProblem(sourceCaptor.capture(), descriptionCaptor.capture(), anyVararg())).
            thenReturn(this.newProblem(sourceCaptor.getValue(), descriptionCaptor.getValue()));
        when(issueCollector.addWarning(sourceCaptor.capture(), descriptionCaptor.capture(), anyVararg())).
            thenReturn(this.newWarning(sourceCaptor.getValue(), descriptionCaptor.getValue()));
        return issueCollector;
    }

    @Override
    public IssueCollector newIssueCollector(Class sourceType) {
        return this.newIssueCollector();
    }

    @Override
    public Problem newProblem(Object source, String description, Object... arguments) {
        Problem problem = mock(Problem.class);
        when(problem.getTimestamp()).thenReturn(Instant.now());
        when(problem.isProblem()).thenReturn(true);
        when(problem.isWarning()).thenReturn(false);
        when(problem.getDescription()).thenReturn(description);
        return problem;
    }

    @Override
    public Problem newProblem(Object source, Thesaurus thesaurus, MessageSeed description, Object... arguments) {
        return this.newProblem(source, description.getKey(), arguments);
    }

    @Override
    public Warning newWarning(Object source, String description, Object... arguments) {
        Warning warning = mock(Warning.class);
        when(warning.getTimestamp()).thenReturn(Instant.now());
        when(warning.isWarning()).thenReturn(true);
        when(warning.isWarning()).thenReturn(false);
        when(warning.getDescription()).thenReturn(description);
        return warning;
    }

    @Override
    public Warning newWarning(Object source, Thesaurus thesaurus, MessageSeed description, Object... arguments) {
        return this.newWarning(source, description.getKey(), arguments);
    }

}