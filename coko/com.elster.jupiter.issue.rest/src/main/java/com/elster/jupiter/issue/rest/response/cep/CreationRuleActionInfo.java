package com.elster.jupiter.issue.rest.response.cep;

import java.util.List;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleActionInfo {
    public CreationRuleActionPhaseInfo phase;
    public IssueActionTypeInfo type;
    public List<PropertyInfo> properties;

}
