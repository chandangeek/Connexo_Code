package com.elster.jupiter.issue;

public enum IssueAssigneeType {
    USER("U"),
    TEAM("G"),
    ROLE("R");

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
                if (column.name().equalsIgnoreCase(assigneeType)) {
                    return column;
                }
            }
        }
        return null;
    }
}
