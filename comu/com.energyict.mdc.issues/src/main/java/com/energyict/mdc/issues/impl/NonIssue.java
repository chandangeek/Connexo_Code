package com.energyict.mdc.issues.impl;

import java.time.Instant;

/**
 * Implementation of a Non-Issue.
 * Can be used for objects which <i>can</i> contain {@link com.energyict.mdc.issues.Issue}s but
 * currently don't have any.
 *
 * @author gna
 * @since 2/04/12 - 15:04
 */
public class NonIssue extends IssueDefaultImplementation {

    public NonIssue (Instant timestamp) {
        super(timestamp, "NonIssue");
    }

}