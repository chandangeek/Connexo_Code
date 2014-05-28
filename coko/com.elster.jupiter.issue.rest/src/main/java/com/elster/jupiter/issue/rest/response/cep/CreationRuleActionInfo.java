package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.ActionParameter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleActionInfo {
    private long id;
    private CreationRuleActionPhaseInfo phase;
    private Map<String, String> parameters;

    public CreationRuleActionInfo() {}

    public CreationRuleActionInfo(CreationRuleAction action) {
        if (action != null) {
            this.id = action.getId();
            this.phase = new CreationRuleActionPhaseInfo(action.getPhase());
            this.parameters = new HashMap<>(action.getParameters().size());
            for (ActionParameter parameter : action.getParameters()) {
                this.parameters.put(parameter.getKey(), parameter.getValue());
            }
        }
    }

    public long getId() {
        return id;
    }

    public CreationRuleActionPhaseInfo getPhase() {
        return phase;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
