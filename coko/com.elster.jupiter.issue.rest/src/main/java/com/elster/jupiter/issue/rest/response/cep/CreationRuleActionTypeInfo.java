package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.share.entity.CreationRuleActionType;

//TODO fill params
public class CreationRuleActionTypeInfo {

    private long id;
    private String name;
    private String description;
    private IssueTypeInfo issueType;

    public CreationRuleActionTypeInfo(CreationRuleActionType type) {
        if (type != null) {
            this.id = type.getId();
            this.name = type.getName();
            this.description = type.getDescription();
            this.issueType = new IssueTypeInfo(type.getIssueType());
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

    public IssueTypeInfo getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueTypeInfo issueType) {
        this.issueType = issueType;
    }
}
