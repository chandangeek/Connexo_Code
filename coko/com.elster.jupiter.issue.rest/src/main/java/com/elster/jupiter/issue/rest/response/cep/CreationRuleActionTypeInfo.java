package com.elster.jupiter.issue.rest.response.cep;

import java.util.List;

import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleActionTypeInfo {
    public long id;
    public String name;
    public IssueTypeInfo issueType;
    public List<PropertyInfo> properties;

    public CreationRuleActionTypeInfo() {
    }

    public CreationRuleActionTypeInfo(IssueActionType type) {
        this.id = type.getId();
        IssueAction action = type.createIssueAction().get();
        this.name = action.getDisplayName();
        this.issueType = new IssueTypeInfo(type.getIssueType());
        this.properties = new PropertyUtils().convertPropertySpecsToPropertyInfos(action.getPropertySpecs());
    }
}