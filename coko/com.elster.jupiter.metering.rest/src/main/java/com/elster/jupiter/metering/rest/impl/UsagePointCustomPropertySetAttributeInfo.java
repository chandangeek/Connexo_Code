package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointCustomPropertySetAttributeInfo {

    public String name;
    public boolean required;

    public UsagePointCustomPropertySetAttributeInfo() {
    }

    public UsagePointCustomPropertySetAttributeInfo(PropertySpec propertySpec) {
        this();
        this.name = propertySpec.getName();
        this.required = propertySpec.isRequired();
    }
}