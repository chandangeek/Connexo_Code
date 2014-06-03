package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleTemplateInfo {
    private String uid;
    private String name;
    private String description;
    List<ParameterInfo> parameters;

    public CreationRuleTemplateInfo() {}

    public CreationRuleTemplateInfo(CreationRuleTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("CreationRuleTemplateInfo is initialized with the null CreationRuleTemplate value");
        }
        this.uid = template.getUUID();
        this.name = template.getName();
        this.description = template.getDescription();
        if (template.getParameterDefinitions() != null) {
            this.parameters = new ArrayList<>();
            for (ParameterDefinition parameter : template.getParameterDefinitions().values()) {
                parameters.add(new ParameterInfo(parameter));
            }
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public List<ParameterInfo> getParameters() {
        return parameters;
    }
}
