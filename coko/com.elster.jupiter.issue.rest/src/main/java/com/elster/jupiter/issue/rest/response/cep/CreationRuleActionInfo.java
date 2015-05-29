package com.elster.jupiter.issue.rest.response.cep;

import java.util.List;

import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleActionInfo {
    public CreationRuleActionPhaseInfo phase;
    public CreationRuleActionTypeInfo type;
    public List<PropertyInfo> properties;

    public CreationRuleActionInfo() {
    }

    public CreationRuleActionInfo(CreationRuleAction action) {
        this.phase = new CreationRuleActionPhaseInfo(action.getPhase());
        this.type = new CreationRuleActionTypeInfo(action.getAction());
        this.properties = new PropertyUtils().convertPropertySpecsToPropertyInfos(action.getPropertySpecs(), action.getProps());
    }
}
