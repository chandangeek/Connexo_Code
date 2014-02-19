package com.elster.jupiter.issue;

/**
 This enumeration provides list of available statuses for issue type
 */
public enum IssueStatus {
    OPEN ("Open"),
    CLOSED ("Closed"),
    POSTPONED ("Postponded"),
    IN_PROGRESS ("In progress"),
    REJECTED ("Rejected");

    private final String readableName;

    IssueStatus(String name) {
        this.readableName = name;
    }

    public String toString() {
        return this.readableName;
    }
}
