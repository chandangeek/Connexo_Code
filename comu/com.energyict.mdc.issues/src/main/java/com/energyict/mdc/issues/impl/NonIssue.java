package com.energyict.mdc.issues.impl;

import java.util.Date;

/**
 * Implementation of a Non-Issue. Can be used for objects which <i>can</i> contain {@link com.energyict.mdc.issues.Issue}s but
 * currently don't have any.
 *
 * @author gna
 * @since 2/04/12 - 15:04
 */
public class NonIssue<S> extends IssueDefaultImplementation<S> {

    public NonIssue (Date timestamp) {
        super(timestamp, "NonIssue");
    }

}