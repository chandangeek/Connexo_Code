package com.elster.jupiter.issue.share.entity;

public enum CreationRuleActionPhase {
    CREATE("IssueActionPhaseCreation"),
    OVERDUE("IssueActionPhaseOverdue");

    private String titleId;

    CreationRuleActionPhase(String titleId) {
        this.titleId = titleId;
    }

    public String getTitleId(){
        return titleId;
    }

    public static CreationRuleActionPhase fromString(String phase) {
        if (phase != null) {
            for (CreationRuleActionPhase column : CreationRuleActionPhase.values()) {
                if (column.name().equalsIgnoreCase(phase)) {
                    return column;
                }
            }
        }
        return null;
    }
}
