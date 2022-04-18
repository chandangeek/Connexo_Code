/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.i18n;

import com.elster.jupiter.issue.task.impl.ModuleConstants;
import com.elster.jupiter.issue.task.impl.TaskIssueProcessAssociationProvider;
import com.elster.jupiter.issue.task.impl.actions.CloseIssueAction;
import com.elster.jupiter.issue.task.impl.templates.BasicTaskIssueRuleTemplate;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    AQ_TASK_EVENT_SUBSC(ModuleConstants.AQ_TASK_EVENT_SUBSC, ModuleConstants.AQ_TASK_EVENT_DISPLAYNAME),
    BASIC_TEMPLATE_TASK_NAME("TemplateBasicTaskIssueName", "Create issue when specific task event occurs"),
    BASIC_TEMPLATE_TASK_DESCRIPTION("TemplateBasicTaskIssueDescription", "Create issue when specific task event occurs"),
    TASK_FAILED_EVENT(ModuleConstants.TASK_FAILED_EVENT, "TaskFailedEvent"),
    LOG_ON_SAME_ISSUE(BasicTaskIssueRuleTemplate.LOG_ON_SAME_ISSUE, "When recurring"),
    PARAMETER_DO_NOTHING("WhenRecurringDoNothing", "Do nothing"),
    PARAMETER_INCREASE_URGENCY("WhenRecurringIncreaseUrgency", "Increase urgency (+1)"),
    ISSUE_TYPE_TASK("IssueTypeTask", "Task"),
    ISSUE_REASON_TASKFAILED("TaskFailed", "Task failed"),
    ISSUE_REASON_DESCRIPTION_TASKFAILED("IssueReasonTaskFailedDescription", "Task {0} failed"),
    CLOSE_ACTION_PROPERTY_CLOSE_STATUS(CloseIssueAction.CLOSE_STATUS, "Close status"),
    CLOSE_ACTION_PROPERTY_COMMENT(CloseIssueAction.COMMENT, "Comment"),
    CLOSE_ACTION_WRONG_STATUS("action.wrong.status", "You are trying to apply the incorrect status"),
    CLOSE_ACTION_ISSUE_CLOSED("action.issue.closed", "Issue closed"),
    CLOSE_ACTION_ISSUE_ALREADY_CLOSED("action.issue.already.closed", "Issue already closed"),
    CLOSE_ACTION_CLOSE_ISSUE("issue.action.closeIssue", "Close issue"),
    ISSUE_CREATION_SELECTION_ON_RECURRENCE("issue.creation.selection.on.recurrence", "On recurrence"),
    CREATE_NEW_TASK_ISSUE("create.new.task.issue", "Create new issue"),
    LOG_ON_SAME_TASK_ISSUE("log.on.same.task.issue", "Log on existing open issue"),
    TASK_PROPS("task.issue.props", "Task issue properties"),

    TASK_ISSUE_ASSOCIATION_PROVIDER(TaskIssueProcessAssociationProvider.ASSOCIATION_TYPE, "Task issue"),
    TASK_ISSUE_REASON_TITLE("issueReasons", "Issue reasons"),
    TASK_ISSUE_REASON_COLUMN("issueReason", "Issue reason");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}
