package com.elster.jupiter.issue.rest.response.cep;

import java.util.List;

import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleActionTypeInfo {
    public long id;
    public String name;
    public IssueTypeInfo issueType;
    public List<PropertyInfo> properties;

}