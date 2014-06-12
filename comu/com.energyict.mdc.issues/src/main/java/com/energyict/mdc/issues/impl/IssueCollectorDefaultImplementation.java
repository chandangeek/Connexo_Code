package com.energyict.mdc.issues.impl;

import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueCollector;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides a default implementation for the {@link IssueCollector} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public class IssueCollectorDefaultImplementation implements IssueCollector {

    private Clock clock;
    private List<Issue> issues = new ArrayList<>();

    public IssueCollectorDefaultImplementation (Clock clock) {
        super();
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     */
    public void clear () {
        this.issues.clear();
    }

    /**
     * {@inheritDoc}
     */
    public Issue addProblem (String description) {
        return this.addIssue(new ProblemImpl(this.clock.now(), description));
    }

    /**
     * {@inheritDoc}
     */
    public Issue addProblem(Object source, String description, Object... arguments) {
        return this.addIssue(new ProblemImpl(this.clock.now(), source, description));
    }

    /**
     * {@inheritDoc}
     */
    public Issue addWarning (String description) {
        return this.addIssue(new WarningImpl(this.clock.now(), description));
    }

    /**
     * {@inheritDoc}
     */
    public Issue addWarning(Object source, String description, Object... arguments) {
        return this.addIssue(new WarningImpl(this.clock.now(), source, description));
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasIssues () {
        return !this.issues.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasIssues (Object source) {
        return this.hasWarnings(source) || this.hasProblems(source);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasWarnings () {
        return !this.getWarnings().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasWarnings (Object source) {
        return !this.getWarnings(source).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasProblems () {
        return !this.getProblems().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasProblems (Object source) {
        return !this.getProblems(source).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<? extends Issue> getIssues () {
        return new ArrayList<>(this.issues);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<? extends Issue> getIssues (Object source) {
        Collection<Issue> sourceIssues = new ArrayList<>(this.issues.size());
        for (Issue issue : this.issues) {
            if (Checks.is(source).equalTo(issue.getSource())) {
                sourceIssues.add(issue);
            }
        }
        return sourceIssues;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<? extends Warning> getWarnings () {
        return this.filterWarnings(this.issues);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<? extends Warning> getWarnings (Object source) {
        return this.filterWarnings(this.getIssues(source));
    }

    /**
     * {@inheritDoc}
     */
    public Collection<? extends Problem> getProblems () {
        return this.filterProblems(this.issues);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<? extends Problem> getProblems (Object source) {
        return this.filterProblems(this.getIssues(source));
    }

    /**
     * Adds the specified {@link Issue} to this IssueCollector.
     *
     * @param issue The Issue
     * @return The Issue
     */
    private Issue addIssue (Issue issue) {
        this.issues.add(issue);
        return issue;
    }

    /**
     * Filters all {@link WarningImpl}s contained in the Collection of {@link Issue}s.
     *
     * @param unfilteredIssues The Issues
     * @return The Warnings
     */
    private Collection<? extends Warning> filterWarnings (Collection<? extends Issue> unfilteredIssues) {
        Collection<Warning> warnings = new ArrayList<>(unfilteredIssues.size());    // At most all issues are warnings.
        for (Issue issue : unfilteredIssues) {
            if (issue.isWarning()) {
                warnings.add((Warning) issue);
            }
        }
        return warnings;
    }

    /**
     * Filters all {@link ProblemImpl}s contained in the Collection of {@link Issue}s.
     *
     * @param unfilteredIssues The Issues
     * @return The Problems
     */
    private Collection<? extends Problem> filterProblems (Collection<? extends Issue> unfilteredIssues) {
        Collection<Problem> problems = new ArrayList<>(unfilteredIssues.size());    // At most all issues are problems.
        for (Issue issue : unfilteredIssues) {
            if (issue.isProblem()) {
                problems.add((Problem) issue);
            }
        }
        return problems;
    }

}