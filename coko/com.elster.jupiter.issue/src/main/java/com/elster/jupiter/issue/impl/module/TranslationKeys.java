/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.issue.impl.actions.CommentIssueAction;
import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.tasks.IssueOverdueHandlerFactory;
import com.elster.jupiter.issue.impl.tasks.IssueSnoozeHandlerFactory;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-03 (08:45)
 */
public enum TranslationKeys implements TranslationKey {

    ISSUE_STATUS_OPEN("issue.status.open", "Created"),
    ISSUE_STATUS_SNOOZED("issue.status.snoozed", "Snoozed"),
    ISSUE_STATUS_RESOLVED("issue.status.resolved", "Resolved"),
    ISSUE_STATUS_WONT_FIX("issue.status.wont.fix", "Won''t fix"), // intentionally two '
    ISSUE_STATUS_FORWARDED("issue.status.forwarded", "Forwarded"),
    ISSUE_STATUS_IN_PROGRESS("issue.status.in.progress", "Ongoing"),
    ACTION_ASSIGN_ISSUE("issue.action.assignIssue", "Assign issue"),
    ACTION_COMMENT_ISSUE("issue.action.commentIssue", "Comment issue"),
    CLOSE_ACTION_CLOSE_ISSUE("issue.action.closeIssue", "Close issue"),
    ACTION_MAIL_ISSUE("issue.action.email", "Email"),
    ACTION_MAIL_TO("issue.action.mail.to", "To"),
    ACTION_RETRY_NOW("ActionRetryNow", "Retry now"),
    ACTION_RETRY("ActionRetry", "Retry"),
    COMMENTACTION_PROPERTY_COMMENT(CommentIssueAction.ISSUE_COMMENT, "Comment"),
    UNASSIGNED(DatabaseConst.UNASSIGNED, "Unassigned"),
    SUBSCRIBER_NAME(IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_SUBSCRIBER, IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_DISPLAYNAME),
    ISSUE_SNOOZE_SUBSCRIBER_NAME (IssueSnoozeHandlerFactory.ISSUE_SNOOZE_TASK_SUBSCRIBER, IssueSnoozeHandlerFactory.ISSUE_SNOOZE_TASK_DISPLAYNAME),
    ACTION_ISSUE_ASSIGNED("action.issue.assigned", "Issue assigned"),
    ACTION_ISSUE_COMMENTED("action.issue.commented", "Issue commented"),
    ACTION_ISSUE_SNOOZED("action.issue.snoozed", "Snoozed"),
    ACTION_ISSUE_UNASSIGNED("action.issue.unassigned", "Issue unassigned"),
    ACTION_WEBSERVICE_NOTIFICATION_CALLED("issue.action.webServiceNotificationCalled", "Web service notification called"),
    ACTION_WEBSERVICE_NOTIFICATION("issue.action.webServiceNotification", "Web service notification"),
    ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ISSUE("issue.action.webServiceNotification.closeIssue", "Close issue"),
    ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ISSUE_DESCRIPTION("issue.action.webServiceNotification.close.description", "Select to remove the issue from operational screens in Connexo (e.g. dashboard, overviews, what''s going on). It will receive the status ''Forwarded''.");

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