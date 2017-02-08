/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.IssueAssignee;

public class AssignmentRuleInfo {
    private long id;
    private String name;
    private String description;
    private IssueAssigneeInfo assignee;
    private long version;

    public AssignmentRuleInfo(AssignmentRule rule) {
        if (rule != null) {
            this.id = rule.getId();
            this.name = rule.getTitle();
            this.description = rule.getDescription();
            this.version = rule.getVersion();
            IssueAssignee issueAssignee = rule.getAssignee();
            if (issueAssignee != null) {
                this.assignee = new IssueAssigneeInfo(issueAssignee);
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueAssigneeInfo getAssignee() {
        return assignee;
    }

    public void setAssignee(IssueAssigneeInfo assignee) {
        this.assignee = assignee;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
