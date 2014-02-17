package com.elster.jupiter.issue;

public interface IssueAssignee {
    long getId();
    IssueAssigneeType getAssigneeType();
    // TODO here should be a valid reference to Assignee object
    String getAssigneeRef();
    long getVersion();
}
