package com.energyict.mdc.issues.impl;

import com.energyict.mdc.issues.Problem;

import java.util.Date;

/**
 * Models an {@link com.energyict.mdc.issues.Issue Issue} that cannot be worked around
 * without making changes to the information that caused the problem.
 *
 * @param <S> The type of source object
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public class ProblemImpl<S> extends IssueDefaultImplementation<S> implements Problem<S> {

    public ProblemImpl (Date timestamp, String description) {
        super(timestamp, description);
    }

    public ProblemImpl (Date timestamp, S source, String description, Object... arguments) {
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