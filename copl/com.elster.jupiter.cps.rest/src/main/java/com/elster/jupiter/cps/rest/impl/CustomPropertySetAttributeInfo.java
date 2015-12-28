package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public CustomPropertySetAttributeInfo(PropertySpec propertySpec, Thesaurus thesaurus) {
        this.name = propertySpec.getName();
        this.type = propertySpec.getValueFactory().getValueType().getName();
        this.typeSimpleName = thesaurus.getString(propertySpec.getValueFactory().getValueType().getName(), propertySpec.getValueFactory().getValueType().getName());
        this.required = propertySpec.isRequired();
        this.description = propertySpec.getDescription();
        Optional<Object> defaultValue = Optional.ofNullable(propertySpec.getPossibleValues().getDefault());
        if (defaultValue.isPresent()) {
            if (defaultValue.get() instanceof Boolean) {
                this.defaultValue = thesaurus.getString(defaultValue.get().toString(), defaultValue.get().toString());
            } else {
                this.defaultValue = defaultValue.get();
            }
        } else {
            this.defaultValue = "";
        }
        Optional<List<Object>> possibleValues = Optional.ofNullable(propertySpec.getPossibleValues().getAllValues());
        if (possibleValues.isPresent()) {
            this.allValues = possibleValues.get();
        } else {
            this.allValues = new ArrayList<>();
        }
    }
}