package com.energyict.mdc.issues;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.issue.Warning;

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
    IssueCollector newIssueCollector();

    /**
     * Creates a new {@link IssueCollector} that is ready
     * to start collecting {@link Issue}s for the specified type.
     *
     * @param sourceType The type against which Issues will be created
     * @return The new IssueCollector
     */
    IssueCollector newIssueCollector(Class sourceType);

    /**
     * Creates a new {@link Problem} with the specified description
     * that is caused by the specified source object.
     *
     * @param source      The object that caused the problem
     * @param description The {@link MessageSeed} holding the translation key and default format to describe the problem
     * @param arguments   Additional arguments to put into the description
     */
    Problem newProblem(Object source, MessageSeed description, Object... arguments);

    /**
     * Creates a new {@link Warning} with the specified description
     * that is caused by the specified source object.
     *
     * @param source      The object that caused the warning
     * @param description The {@link MessageSeed} holding the translation key and default format to describe the warning message
     * @param arguments   Additional arguments to put into the description
     */
    Warning newWarning(Object source, MessageSeed description, Object... arguments);

}