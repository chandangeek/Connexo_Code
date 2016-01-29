package com.elster.jupiter.cps.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetAttributeInfo {

    public String name;
    public String displayName;
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
        this.displayName = propertySpec.getDisplayName();
        this.type = propertySpec.getValueFactory().getValueType().getName();
        this.typeSimpleName = thesaurus.getString(this.type, this.type);
        this.required = propertySpec.isRequired();
        this.description = propertySpec.getDescription();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues != null) {
            Optional<Object> defaultValue = Optional.ofNullable(possibleValues.getDefault());
            if (defaultValue.isPresent()) {
                if (defaultValue.get() instanceof Boolean) {
                    this.defaultValue = thesaurus.getString(defaultValue.get().toString(), defaultValue.get().toString());
                } else {
                    this.defaultValue = defaultValue.get();
                }
            } else {
                this.defaultValue = "";
            }
            List<Object> values = possibleValues.getAllValues();
            this.allValues = values != null ? values : Collections.emptyList();
        }
    }
}