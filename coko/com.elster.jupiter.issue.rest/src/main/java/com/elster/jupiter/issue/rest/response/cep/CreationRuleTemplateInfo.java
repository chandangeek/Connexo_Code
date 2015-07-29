package com.elster.jupiter.issue.rest.response.cep;

import java.util.List;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleTemplateInfo {
    public String name;
    public String displayName;
    public String description;
    public List<PropertyInfo> properties;

}
