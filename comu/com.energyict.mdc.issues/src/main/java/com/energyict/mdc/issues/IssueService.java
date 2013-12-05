package com.energyict.mdc.issues;

/**
 * Provides services to collect {@link Issue}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (11:29)
 */
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
     * @param <S> The type against which Issues will be created
     * @return The new IssueCollector
     */
    public <S> IssueCollector<S> newIssueCollector (Class<S> sourceType);

    /**
     * Creates a new {@link Problem} with the specified description
     * that is caused by the specified source object.
     *
     * @param source The object that caused the problem
     * @param description A description that can be translated (can contain an optional pattern to put in some arguments)
     * @param arguments Additional arguments to put into the description
     * @param <S> The type of the source object that caused the problem
     */
    public <S> Problem<S> newProblem (S source, String description, Object... arguments);

    /**
     * Creates a new {@link Warning} with the specified description
     * that is caused by the specified source object.
     *
     * @param source The object that caused the warning
     * @param description A description that can be translated (can contain an optional pattern to put in some arguments)
     * @param arguments Additional arguments to put into the description
     * @param <S> The type of the source object that caused the warning
     */
    public <S> Warning<S> newWarning (S source, String description, Object... arguments);

}