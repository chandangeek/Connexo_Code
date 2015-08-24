package com.energyict.mdc.issues;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

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
    IssueCollector newIssueCollector ();

    /**
     * Creates a new {@link IssueCollector} that is ready
     * to start collecting {@link Issue}s for the specified type.
     *
     * @param sourceType The type against which Issues will be created
     * @return The new IssueCollector
     */
    IssueCollector newIssueCollector (Class sourceType);

    /**
     * Creates a new {@link Problem} with the specified description
     * that is caused by the specified source object.
     * Will be removed once jira issue https://jira.eict.vpdc/browse/COMU-1936 is resolved.
     *
     * @param source The object that caused the problem
     * @param description A description that can be translated (can contain an optional pattern to put in some arguments)
     * @param arguments Additional arguments to put into the description
     * @deprecated Use {@link #newProblem(Object, Thesaurus, MessageSeed, Object...)} instead
     */
    @Deprecated
    Problem newProblem (Object source, String description, Object... arguments);

    /**
     * Creates a new {@link Problem} with the specified description
     * that is caused by the specified source object.
     *
     * @param source The object that caused the problem
     * @param thesaurus The Thesaurus that holds the translation of all descriptions
     * @param description A description that can be translated (can contain an optional pattern to put in some arguments)
     * @param arguments Additional arguments to put into the description
     */
    Problem newProblem (Object source, Thesaurus thesaurus, MessageSeed description, Object... arguments);

    /**
     * Creates a new {@link Warning} with the specified description
     * that is caused by the specified source object.
     * Will be removed once jira issue https://jira.eict.vpdc/browse/COMU-1936 is resolved.
     *
     * @param source The object that caused the warning
     * @param description A description that can be translated (can contain an optional pattern to put in some arguments)
     * @param arguments Additional arguments to put into the description
     * @deprecated Use {@link #newWarning(Object, Thesaurus, MessageSeed, Object...)} instead
     */
    @Deprecated
    Warning newWarning (Object source, String description, Object... arguments);

    /**
     * Creates a new {@link Warning} with the specified description
     * that is caused by the specified source object.
     *
     * @param source The object that caused the warning
     * @param thesaurus The Thesaurus that holds the translation of all descriptions
     * @param description A description that can be translated (can contain an optional pattern to put in some arguments)
     * @param arguments Additional arguments to put into the description
     */
    Warning newWarning (Object source, Thesaurus thesaurus, MessageSeed description, Object... arguments);

}