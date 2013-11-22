package com.energyict.mdc.rest.impl.properties;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:37
 */
@XmlRootElement
public class PropertyInfo {

    public String key;
    public PropertyValueInfo propertyValueInfo;
    public PropertyTypeInfo propertyTypeInfo;
    public boolean required;

    /**
     * Default constructor 4 JSON deserialization
     */
    public PropertyInfo() {
    }

    public PropertyInfo(String key, PropertyValueInfo propertyValueInfo, PropertyTypeInfo propertyTypeInfo, boolean required) {
        this.key = key;
        this.propertyValueInfo = propertyValueInfo;
        this.propertyTypeInfo = propertyTypeInfo;
        this.required = required;
    }

    public String getKey() {
        return key;
    }

    public PropertyTypeInfo getPropertyTypeInfo() {
        return propertyTypeInfo;
    }

    public boolean isRequired() {
        return required;
    }

    public PropertyValueInfo getPropertyValueInfo() {
        return propertyValueInfo;
    }
}
