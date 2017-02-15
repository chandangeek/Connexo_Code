/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;


public enum CreationRuleActionPhase {
    CREATE("IssueActionPhaseCreation", "IssueActionPhaseCreationDescription"),
    OVERDUE("IssueActionPhaseOverdue", "IssueActionPhaseOverdueDescription");

    private String titleId;
    private String descriptionId;

    CreationRuleActionPhase(String titleId, String descriptionId) {
        this.titleId = titleId;
        this.descriptionId = descriptionId;
    }

    public String getTitleId(){
        return titleId;
    }

    public String getDescriptionId(){
        return descriptionId;
    }

    public static CreationRuleActionPhase fromString(String phase) {
        for (CreationRuleActionPhase column : CreationRuleActionPhase.values()) {
            if (column.name().equalsIgnoreCase(phase)) {
                return column;
            }
        }
        return null;
    }
}
