package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
//TODO set correct fields
public class CreationRuleActionInfo {
    private String type;

    public CreationRuleActionInfo() {}

    public CreationRuleActionInfo(CreationRuleAction action) {
        if (action != null) {
            this.type = action.getType().getName();
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
