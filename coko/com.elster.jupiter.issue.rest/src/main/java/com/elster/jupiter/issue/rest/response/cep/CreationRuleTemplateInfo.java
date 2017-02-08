/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.properties.rest.PropertyInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleTemplateInfo {
    public String name;
    public String displayName;
    public String description;
    public List<PropertyInfo> properties;

}
