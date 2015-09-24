package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.properties.PropertySpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomAttributeSetAttributeInfo {

    public String name;
    public String type;
    public String typeSimpleName;
    public String defaultValue;
    public String description;
    public List<String> allValues;
    public boolean required;

    public CustomAttributeSetAttributeInfo() {
    }

    public CustomAttributeSetAttributeInfo(PropertySpec propertySpec) {
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

    public static List<CustomAttributeSetAttributeInfo> from(List<PropertySpec> propertySpecs) {
        List<CustomAttributeSetAttributeInfo> customAttributeSetAttributeInfos = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            customAttributeSetAttributeInfos.add(new CustomAttributeSetAttributeInfo(propertySpec));
        }
        return customAttributeSetAttributeInfos;
    }
}