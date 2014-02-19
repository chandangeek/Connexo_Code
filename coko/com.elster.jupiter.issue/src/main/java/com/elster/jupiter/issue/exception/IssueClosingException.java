package com.elster.jupiter.issue.exception;

import com.elster.jupiter.issue.module.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when we unable to close current issue
 */
public class IssueClosingException extends BaseException {

    public IssueClosingException(String issueTitle){
        super(MessageSeeds.ISSUE_ACTION_CLOSE_FAIL, issueTitle);
    }

    /**
     * @param ex the cause
     */
    public IssueClosingException(Exception ex, String issueTitle) {
        super(MessageSeeds.ISSUE_ACTION_CLOSE_FAIL, ex, issueTitle);
    }
}
