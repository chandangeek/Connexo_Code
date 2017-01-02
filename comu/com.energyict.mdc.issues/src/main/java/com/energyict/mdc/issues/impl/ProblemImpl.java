package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.upl.issue.Problem;

import java.time.Instant;

/**
 * Models an {@link com.energyict.mdc.upl.issue.Issue Issue} that cannot be worked around
 * without making changes to the information that caused the problem.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public class ProblemImpl extends IssueDefaultImplementation implements Problem {

    public ProblemImpl(Thesaurus thesaurus, Instant timestamp, String description) {
        super(thesaurus, timestamp, description);
    }

    public ProblemImpl(Thesaurus thesaurus, Instant timestamp, Object source, String description, Object... arguments) {
        super(thesaurus, timestamp, source, description, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProblem() {
        return true;
    }

}