package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplateParameter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleTemplateInfo {
    private String uid;
    private String name;
    private String description;
    private Map<String,CreationRuleTemplateParameterInfo> parameters;

    public CreationRuleTemplateInfo() {}

    public CreationRuleTemplateInfo(CreationRuleTemplate template) {
        if (template != null) {
            this.uid = template.getUUID();
            this.name = template.getName();
            this.description = template.getDescription();
            if (template.getParameters() != null) {
                this.parameters = new HashMap<>();
                for (CreationRuleTemplateParameter parameter : template.getParameters()) {
                    parameters.put(parameter.getName(), new CreationRuleTemplateParameterInfo(parameter));
                }
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

    public Map<String, CreationRuleTemplateParameterInfo> getParameters() {
        return parameters;
    }
}
