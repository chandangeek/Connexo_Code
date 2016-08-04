package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.issue.impl.actions.AssignIssueAction;
import com.elster.jupiter.issue.impl.actions.CommentIssueAction;
import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-03 (08:45)
 */
public enum TranslationKeys implements TranslationKey {

    ISSUE_STATUS_OPEN("issue.status.open", "Created"),
    ISSUE_STATUS_RESOLVED("issue.status.resolved", "Resolved"),
    ISSUE_STATUS_WONT_FIX("issue.status.wont.fix", "Won't fix"),
    ISSUE_STATUS_IN_PROGRESS("issue.status.in.progress", "Ongoing"),
    ACTION_ASSIGN_ISSUE("issue.action.assignIssue", "Assign issue"),
    ACTION_COMMENT_ISSUE("issue.action.commentIssue", "Comment issue"),
    CLOSE_ACTION_CLOSE_ISSUE("issue.action.closeIssue", "Close issue"),
    ACTION_RETRY_NOW("ActionRetryNow", "Retry now"),
    ACTION_RETRY("ActionRetry", "Retry"),
    ASSIGNACTION_PROPERTY_ASSIGNEE(AssignIssueAction.ASSIGNEE, "Assignee"),
    ASSIGNEACTION_PROPERTY_COMMENT(AssignIssueAction.COMMENT, "Comment"),
    COMMENTACTION_PROPERTY_COMMENT(CommentIssueAction.ISSUE_COMMENT, "Comment"),
    UNASSIGNED(DatabaseConst.UNASSIGNED, "Unassigned");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}