/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    ISSUE_ASSIGNEE_ME ("IssueAssigneeMe", "Me"),
    ISSUE_ASSIGNEE_UNASSIGNED ("IssueAssigneeUnassigned", "Unassigned"),
    ISSUE_ACTION_PHASE_CREATE("IssueActionPhaseCreation", "At creation time"),
    ISSUE_ACTION_PHASE_OVERDUE("IssueActionPhaseOverdue", "At overdue time"),
    ISSUE_ACTION_PHASE_CREATE_DESCRIPTION("IssueActionPhaseCreationDescription", "The action will be performed when the issue is created"),
    ISSUE_ACTION_PHASE_OVERDUE_DESCRIPTION("IssueActionPhaseOverdueDescription", "The action will be performed when the issue becomes overdue"),
    ISSUE_ACTION_PHASE_NOT_APPLICABLE_DESCRIPTION("IssueActionPhaseNotApplicableDescription", "The action will be never performed"),
    ISSUE_ACTION_UNASSIGNED("action.issue.unassigned", "Issue unassigned"),
    ISSUE_ACTION_ASSIGNED("action.issue.assigned.user", "Issue assigned"),
    ISSUE_ACTION_PRIORITY_CHANGED("action.issue.priority.changed", "Issue priority changed"),
    ISSUE_ACTION_SNOOZED("action.issue.snoozed", "Issue snoozed");

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

    public static TranslationKeys from(String key) {
        if (key != null) {
            for (TranslationKeys translationKey : TranslationKeys.values()) {
                if (translationKey.getKey().equals(key)) {
                    return translationKey;
                }
            }
        }
        return null;
    }

}