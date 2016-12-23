package com.energyict.mdc.upl.issue;

/**
 * IssueFactory is responsible for the creation of new {@link Issue}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public interface IssueFactory {

    /**
     * Create a new {@link Warning} with the specified human readable description.
     *
     * @param description The translatable description of the Warning
     * @return The Warning that was created
     */
    Issue createWarning(String description);

    /**
     * Create a new {@link Warning} caused by the source object
     * with the specified description.
     *
     * @param source The cause of the warning
     * @param description The translatable description of the Warning (can contain a default pattern to put in the arguments)
     * @param arguments   additional arguments to put into the description
     * @return The Warning that was created
     */
    Issue createWarning(Object source, String description, Object... arguments);

    /**
     * Create a new {@link Problem} with the specified description.
     *
     * @param description The translatable description of the Problem
     * @return The Problem that was created
     */
    Issue createProblem(String description);

    /**
     * Create a new {@link Problem} caused by the source object
     * with the specified human readable description.
     *
     * @param source The cause of the problem
     * @param description The translatable description of the Problem (can contain a default pattern to put in the arguments)
     * @param arguments   additional arguments to put into the description
     * @return The Problem that was created
     */
    Issue createProblem(Object source, String description, Object... arguments);

}