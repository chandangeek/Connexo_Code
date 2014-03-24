package com.elster.jupiter.issue.rest.response.rules;

import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.share.entity.Rule;

public class AssignmentRuleInfo {
    private long id;
    private String name;
    private String description;
    private IssueAssigneeInfo assignee;
    private long version;

    public AssignmentRuleInfo(Rule rule) {
        if (rule != null) {
            setId(rule.getId());
            setName(rule.getTitle());
            setDescription(rule.getDescription());
            setVersion(rule.getVersion());
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
