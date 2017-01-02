package com.energyict.mdc.issues;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.issue.Warning;

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
     void clear ();

    /**
     * Add a new {@link Warning} with the specified human readable description.
     *
     * @param description The translatable description of the Warning
     * @return The Warning that was created and added
     *
     * if translation is needed, use addWarning(MessageSeed)
     */
    Issue addWarning (String description);

    Issue addWarning (MessageSeed descriptionSeed);

    /**
     * Add a new {@link Warning} caused by the source object
     * with the specified description.
     *
     * @param source The cause of the warning
     * @param description The translatable description of the Warning (can contain a default pattern to put in the arguments)
     * @param arguments   additional arguments to put into the description
     * @return The Warning that was created and added
     *
     * if translation is needed, use addWarning(Source source, MessageSeed descriptionSeed, arguments)
     */
    Issue addWarning(Object source, String description, Object... arguments);

    Issue addWarning (Object source, MessageSeed descriptionSeed, Object... arguments);

    /**
     * Add a new {@link Problem} with the specified description.
     *
     * @param description The translatable description of the Problem
     * @return The Problem that was created and added
     *
     * if translation is needed, use addProblem(MessageSeed)
     */
    Issue addProblem(String description);

    Issue addProblem (MessageSeed descriptionSeed);

    /**
     * Add a new {@link Problem} caused by the source object
     * with the specified human readable description.
     *
     * @param source The cause of the problem
     * @param description The translatable description of the Problem (can contain a default pattern to put in the arguments)
     * @param arguments   additional arguments to put into the description
     * @return The Problem that was created and added
     *
     * if translation is needed, use addProblem(Source source, MessageSeed descriptionSeed, arguments)
     */
    Issue addProblem(Object source, String description, Object... arguments);

    Issue addProblem (Object source, MessageSeed descriptionSeed, Object... arguments);

    /**
     * Tests if {@link Issue}s were reported to this IssueCollector.
     *
     * @return A flag that indicates if Issues were reported
     */
    boolean hasIssues();

    /**
     * Tests if {@link Issue}s were reported to this IssueCollector
     * that were caused by the specified source object.
     *
     * @param source The source object
     * @return A flag that indicates if Issues were reported
     */
    boolean hasIssues(Object source);

    /**
     * Tests if {@link Warning}s were reported to this IssueCollector.
     *
     * @return A flag that indicates if Warnings were reported
     */
    boolean hasWarnings();

    /**
     * Tests if {@link Warning}s were reported to this WarningCollector
     * that were caused by the specified source object.
     *
     * @param source The source object
     * @return A flag that indicates if Warnings were reported
     */
    boolean hasWarnings(Object source);

    /**
     * Tests if {@link Problem}s were reported to this IssueCollector.
     *
     * @return A flag that indicates if Problems were reported
     */
    boolean hasProblems();

    /**
     * Tests if {@link Problem}s were reported to this ProblemCollector
     * that were caused by the specified source object.
     *
     * @param source The source object
     * @return A flag that indicates if Problems were reported
     */
    boolean hasProblems(Object source);

    /**
     * Gets the List of {@link Issue}s that were collected.
     *
     * @return The List of Issue objects that were collected
     */
    Collection<? extends Issue> getIssues();

    /**
     * Gets the List of {@link Issue}s that were collected
     * and caused by the specified source object.
     *
     * @param source The source object
     * @return The List of Issue objects that were collected
     */
    Collection<? extends Issue> getIssues(Object source);

    /**
     * Gets the List of {@link Warning}s that were collected.
     *
     * @return The List of Warning objects that were collected
     */
    Collection<? extends Warning> getWarnings();

    /**
     * Gets the List of {@link Warning}s that were collected
     * and caused by the specified source object.
     *
     * @param source The source object
     * @return The List of Warning objects that were collected
     */
    Collection<? extends Warning> getWarnings(Object source);

    /**
     * Gets the List of {@link Problem}s that were collected.
     *
     * @return The List of Problem objects that were collected
     */
    Collection<? extends Problem> getProblems();

    /**
     * Gets the List of {@link Problem}s that were collected
     * and caused by the specified source object.
     *
     * @param source The source object
     * @return The List of Problem objects that were collected
     */
    Collection<? extends Problem> getProblems(Object source);

}