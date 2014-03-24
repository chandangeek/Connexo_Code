package com.elster.jupiter.issue.share.entity;

public enum IssueAssigneeType {
    USER("USER"),
    TEAM("GROUP"),
    ROLE("ROLE");

    private String type;

    private IssueAssigneeType(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }

    public static IssueAssigneeType fromString(String assigneeType) {
        if (assigneeType != null) {
            for (IssueAssigneeType column : IssueAssigneeType.values()) {
                if (column.getType().equalsIgnoreCase(assigneeType)) {
                    return column;
                }
            }
        }
        return null;
    }

    public String toString() {
        return type.toUpperCase();
    }
}
