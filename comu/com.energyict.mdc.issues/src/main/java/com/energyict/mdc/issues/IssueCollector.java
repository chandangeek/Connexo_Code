package com.energyict.mdc.issues;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;

/**
 * IssueCollector is responsible for collecting {@link Issue}s that are found by a process.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
@ProviderType
public interface IssueCollector {

    /**
     * Clears the {@link Issue}s that have already been collected so far.
     */
    public void clear ();

    /**
     * Add a new {@link Warning} with the specified human readable description.
     *
     * @param description The translatable description of the Warning
     * @return The Warning that was created and added
     */
    public Issue addWarning (String description);

    /**
     * Add a new {@link Warning} caused by the source object
     * with the specified description.
     *
     * @param source The cause of the warning
     * @param description The translatable description of the Warning (can contain a default pattern to put in the arguments)
     * @param arguments   additional arguments to put into the description
     * @return The Warning that was created and added
     */
    public Issue addWarning(Object source, String description, Object... arguments);

    /**
     * Add a new {@link Problem} with the specified description.
     *
     * @param description The translatable description of the Problem
     * @return The Problem that was created and added
     */
    public Issue addProblem (String description);

    /**
     * Add a new {@link Problem} caused by the source object
     * with the specified human readable description.
     *
     * @param source The cause of the problem
     * @param description The translatable description of the Problem (can contain a default pattern to put in the arguments)
     * @param arguments   additional arguments to put into the description
     * @return The Problem that was created and added
     */
    public Issue addProblem(Object source, String description, Object... arguments);

    /**
     * Tests if {@link Issue}s were reported to this IssueCollector.
     *
     * @return A flag that indicates if Issues were reported
     */
    public boolean hasIssues ();

    /**
     * Tests if {@link Issue}s were reported to this IssueCollector
     * that were caused by the specified source object.
     *
     * @param source The source object
     * @return A flag that indicates if Issues were reported
     */
    public boolean hasIssues (Object source);

    /**
     * Tests if {@link Warning}s were reported to this IssueCollector.
     *
     * @return A flag that indicates if Warnings were reported
     */
    public boolean hasWarnings ();

    /**
     * Tests if {@link Warning}s were reported to this WarningCollector
     * that were caused by the specified source object.
     *
     * @param source The source object
     * @return A flag that indicates if Warnings were reported
     */
    public boolean hasWarnings (Object source);

    /**
     * Tests if {@link Problem}s were reported to this IssueCollector.
     *
     * @return A flag that indicates if Problems were reported
     */
    public boolean hasProblems ();

    /**
     * Tests if {@link Problem}s were reported to this ProblemCollector
     * that were caused by the specified source object.
     *
     * @param source The source object
     * @return A flag that indicates if Problems were reported
     */
    public boolean hasProblems (Object source);

    /**
     * Gets the List of {@link Issue}s that were collected.
     *
     * @return The List of Issue objects that were collected
     */
    public Collection<? extends Issue> getIssues ();

    /**
     * Gets the List of {@link Issue}s that were collected
     * and caused by the specified source object.
     *
     * @param source The source object
     * @return The List of Issue objects that were collected
     */
    public Collection<? extends Issue> getIssues (Object source);

    /**
     * Gets the List of {@link Warning}s that were collected.
     *
     * @return The List of Warning objects that were collected
     */
    public Collection<? extends Warning> getWarnings ();

    /**
     * Gets the List of {@link Warning}s that were collected
     * and caused by the specified source object.
     *
     * @param source The source object
     * @return The List of Warning objects that were collected
     */
    public Collection<? extends Warning> getWarnings (Object source);

    /**
     * Gets the List of {@link Problem}s that were collected.
     *
     * @return The List of Problem objects that were collected
     */
    public Collection<? extends Problem> getProblems ();

    /**
     * Gets the List of {@link Problem}s that were collected
     * and caused by the specified source object.
     *
     * @param source The source object
     * @return The List of Problem objects that were collected
     */
    public Collection<? extends Problem> getProblems (Object source);

}