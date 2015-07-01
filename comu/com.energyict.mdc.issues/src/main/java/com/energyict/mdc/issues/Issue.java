package com.energyict.mdc.issues;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Models issues that are found by a process while it's executing.
 * Some issues may be fixable or worked around. These are called warnings.<br>
 * Others may be real problems that need changes before the process can be completed successfully.<br>
 * Issues can optionally be reported against a source object, i.e. the object that causes the issue.
 * This could be useful when the process is e.g. a validation process, in which case the source
 * will most likely be the object that has a validation issue.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
@ProviderType
public interface Issue {

    /**
     * Gets the timestamp on which this Issue was created.
     *
     * @return The timestamp on which this Issue was created
     */
    public Instant getTimestamp ();

    /**
     * Gets a human readable description that explains this Issue.
     *
     * @return The human readable description that explains this Issue.
     */
    public String getDescription();

    /**
     * Gets the source object that caused this Issue or <code>null</code>
     * if there was not specific source.
     *
     * @return The object that caused this Issue or <code>null</code>
     */
    public Object getSource();

    /**
     * Tests if this Issue is fixable or can be worked around.
     *
     * @return A flag that indicates if this Issue is fixable or can be worked around
     */
    public boolean isWarning();

    /**
     * Tests if this Issue cannot be worked around.
     *
     * @return A flag that indicates if this Issue cannot be worked around
     */
    public boolean isProblem();

}