package com.elster.jupiter.issue.rest;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    ISSUE_ASSIGNEE_ME ("IssueAssigneeMe", "Me"),
    ISSUE_ASSIGNEE_UNASSIGNED ("IssueAssigneeUnassigned", "Unassigned"),
    ISSUE_ACTION_PHASE_CREATE("IssueActionPhaseCreation", "Issue creation"),
    ISSUE_ACTION_PHASE_OVERDUE("IssueActionPhaseOverdue", "Issue overdue"),
    ISSUE_ACTION_PHASE_CREATE_DESCRIPTION("IssueActionPhaseCreationDescription", "The action will be performed at the issue creation time"),
    ISSUE_ACTION_PHASE_OVERDUE_DESCRIPTION("IssueActionPhaseOverdueDescription", "The action will be performed when the issue becomes overdue")
    ;

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