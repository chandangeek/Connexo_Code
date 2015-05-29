package com.elster.jupiter.issue.rest.response.cep;

import java.util.List;

import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleTemplateInfo {
    public String name;
    public String displayName;
    public String description;
    public List<PropertyInfo> properties;

    public CreationRuleTemplateInfo() {}

    public CreationRuleTemplateInfo(CreationRuleTemplate template) {
        this.name = template.getName();
        this.displayName = template.getDisplayName();
        this.description = template.getDescription();
        this.properties = new PropertyUtils().convertPropertySpecsToPropertyInfos(template.getPropertySpecs());
    }
}
