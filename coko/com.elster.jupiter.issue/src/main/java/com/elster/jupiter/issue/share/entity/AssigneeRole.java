package com.elster.jupiter.issue.share.entity;

public interface AssigneeRole extends IssueAssignee, Entity {
    void setName(String name);

    String getDescription();

    void setDescription(String description);
}
