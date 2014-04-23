package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.entity.CreationRuleParameter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleParameterInfo {
    private String key;
    private String value;

    public CreationRuleParameterInfo() {}

    public CreationRuleParameterInfo(CreationRuleParameter parameter) {
        if (parameter != null) {
            this.key = parameter.getKey();
            this.value = parameter.getValue();
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
