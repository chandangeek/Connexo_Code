/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueCollector;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a default implementation for the {@link IssueCollector} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public class IssueCollectorDefaultImplementation implements IssueCollector {

    private final Clock clock;
    private final Thesaurus thesaurus;
    private List<Issue> issues = new ArrayList<>();

    public IssueCollectorDefaultImplementation(Clock clock, Thesaurus thesaurus) {
        super();
        this.clock = clock;
        this.thesaurus = thesaurus;
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
        return this.addIssue(new ProblemImpl(this.thesaurus, this.clock.instant(), description));
    }

    @Override
    public Issue addProblem(MessageSeed descriptionSeed) {
        return addIssue(new ProblemBackedByMessageSeed(this, this.clock.instant(), thesaurus, descriptionSeed, null));
    }

    /**
     * {@inheritDoc}
     */
    public Issue addProblem(Object source, String description, Object... arguments) {
        return this.addIssue(new ProblemImpl(this.thesaurus, this.clock.instant(), source, description, arguments));
    }

    @Override
    public Issue addProblem(Object source, MessageSeed descriptionSeed, Object... arguments) {
        return addIssue(new ProblemBackedByMessageSeed(source, this.clock.instant(), thesaurus, descriptionSeed, arguments));
    }

    /**
     * {@inheritDoc}
     */
    public Issue addWarning (String description) {
        return this.addIssue(new WarningImpl(this.thesaurus, this.clock.instant(), description));
    }

    public Issue addWarning(MessageSeed descriptionSeed) {
        return addIssue(new WarningBackedByMessageSeed(this, clock.instant(), thesaurus, descriptionSeed, null));
    }

    /**
     * {@inheritDoc}
     */
    public Issue addWarning(Object source, String description, Object... arguments) {
        return this.addIssue(new WarningImpl(this.thesaurus, this.clock.instant(), source, description, arguments));
    }

    public Issue addWarning(Object source, MessageSeed seedDescription, Object... arguments) {
        return addIssue(new WarningBackedByMessageSeed(source, this.clock.instant(), this.thesaurus, seedDescription, arguments));
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
        return this.issues
                .stream()
                .filter(issue -> Checks.is(source).equalTo(issue.getSource()))
                .collect(Collectors.toList());
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