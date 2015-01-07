package com.energyict.mdc.issues.impl;

import com.energyict.mdc.issues.Problem;

import java.time.Instant;

/**
 * Models an {@link com.energyict.mdc.issues.Issue Issue} that cannot be worked around
 * without making changes to the information that caused the problem.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public class ProblemImpl extends IssueDefaultImplementation implements Problem {

    public ProblemImpl (Instant timestamp, String description) {
        super(timestamp, description);
    }

    public ProblemImpl (Instant timestamp, Object source, String description, Object... arguments) {
        super(timestamp, source, description, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProblem() {
        return true;
    }

}