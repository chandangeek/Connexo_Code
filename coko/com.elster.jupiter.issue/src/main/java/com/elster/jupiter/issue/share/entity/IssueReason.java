package com.elster.jupiter.issue.share.entity;

public interface IssueReason extends Entity {

    String getName();

    void setName(String name);

    IssueType getIssueType();

    void setIssueType(IssueType type);
}
