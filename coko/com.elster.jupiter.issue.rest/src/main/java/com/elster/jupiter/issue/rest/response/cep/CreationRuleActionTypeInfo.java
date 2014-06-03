package com.elster.jupiter.issue.rest.response.cep;

import java.util.LinkedHashMap;
import java.util.Map;

import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.entity.IssueActionType;

public class CreationRuleActionTypeInfo {
    private long id;
    private String name;
    private IssueTypeInfo issueType;
    private Map<String, ParameterInfo> parameters;

    public CreationRuleActionTypeInfo(IssueActionType type) {
        if (type == null) {
            throw new IllegalArgumentException("CreationRuleActionTypeInfo is initialized with the null IssueActionType value");
        }
        this.id = type.getId();
        IssueAction action = type.createIssueAction();
        this.name = action.getLocalizedName();
        this.issueType = new IssueTypeInfo(type.getIssueType());
        initParameters(action);
    }

    private final void initParameters(IssueAction action) {
        if (!action.getParameterDefinitions().isEmpty()) {
            parameters = new LinkedHashMap<>();
        }
        for (Map.Entry<String, ParameterDefinition> parameter : action.getParameterDefinitions().entrySet()) {
            parameters.put(parameter.getKey(), new ParameterInfo(parameter.getValue()));
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IssueTypeInfo getIssueType() {
        return issueType;
    }

    public Map<String, ParameterInfo> getParameters() {
        return parameters;
    }
}