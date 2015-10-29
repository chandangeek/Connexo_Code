package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetAttributeInfo {

    public String name;
    public String type;
    public String typeSimpleName;
    public Object defaultValue;
    public String description;
    public List<Object> allValues;
    public boolean required;

    public CustomPropertySetAttributeInfo() {
    }

    public CustomPropertySetAttributeInfo(PropertySpec propertySpec) {
        this.name = propertySpec.getName();
        this.type = propertySpec.getValueFactory().getValueType().getName();
        this.typeSimpleName = propertySpec.getValueFactory().getValueType().getSimpleName();
        this.required = propertySpec.isRequired();
        this.description = propertySpec.getDescription();
        if (propertySpec.getPossibleValues() != null) {
            this.defaultValue = propertySpec.getPossibleValues().getDefault() != null ? propertySpec.getPossibleValues().getDefault().toString() : "";
            this.allValues = propertySpec.getPossibleValues().getAllValues() != null ? propertySpec.getPossibleValues().getAllValues() : new ArrayList<>();
        }
    }
}