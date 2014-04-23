package com.elster.jupiter.issue.share.entity;

public interface CreationRuleActionType extends Entity {

    String getName();
    void setName(String name);

    String getDescription();
    void setDescription(String description);

    String getClassName();
    void setClassName(String className);

    IssueType getIssueType();
    void setIssueType(IssueType type);
}
