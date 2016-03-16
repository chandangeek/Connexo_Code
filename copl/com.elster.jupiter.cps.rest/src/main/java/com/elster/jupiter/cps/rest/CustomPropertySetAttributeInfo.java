package com.elster.jupiter.cps.rest;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetAttributeInfo extends PropertyInfo {
    public CustomPropertySetAttributeTypeInfo propertyTypeInfo;
    public String description;
}