package com.energyict.mdc.issues;

import aQute.bnd.annotation.ProviderType;

/**
 * Provides services to collect {@link Issue}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (11:29)
 */
@ProviderType
public interface IssueService {

    /**
     * Creates a new {@link IssueCollector} that is ready
     * to start collecting {@link Issue}s.
     *
     * @return The new IssueCollector
     */
    public IssueCollector newIssueCollector ();

    /**
     * Creates a new {@link IssueCollector} that is ready
     * to start collecting {@link Issue}s for the specified type.
     *
     * @param sourceType The type against which Issues will be created
     * @return The new IssueCollector
     */
    public  IssueCollector newIssueCollector (Class sourceType);

    /**
     * Creates a new {@link Problem} with the specified description
     * that is caused by the specified source object.
     *
     * @param source The object that caused the problem
     * @param description A description that can be translated (can contain an optional pattern to put in some arguments)
     * @param arguments Additional arguments to put into the description
     */
    public  Problem newProblem (Object source, String description, Object... arguments);

    /**
     * Creates a new {@link Warning} with the specified description
     * that is caused by the specified source object.
     *
     * @param source The object that caused the warning
     * @param description A description that can be translated (can contain an optional pattern to put in some arguments)
     * @param arguments Additional arguments to put into the description
     */
    public  Warning newWarning (Object source, String description, Object... arguments);

}