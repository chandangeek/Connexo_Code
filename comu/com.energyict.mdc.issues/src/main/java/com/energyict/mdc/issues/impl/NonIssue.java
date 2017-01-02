package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;

/**
 * Implementation of a Non-Issue.
 * Can be used for objects which <i>can</i> contain {@link com.energyict.mdc.upl.issue.Issue}s but
 * currently don't have any.
 *
 * @author gna
 * @since 2/04/12 - 15:04
 */
public class NonIssue extends IssueDefaultImplementation {

    public NonIssue(Thesaurus thesaurus, Instant timestamp) {
        super(thesaurus, timestamp, "NonIssue");
    }

}