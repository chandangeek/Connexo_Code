package com.energyict.mdc.rest.impl.properties;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:37
 */
public class PropertyInfo {

    final String key;
    final PropertyValueInfo propertyValueInfo;
    final PropertyTypeInfo propertyTypeInfo;
    final boolean isRequired;

    public PropertyInfo(String key, PropertyValueInfo propertyValueInfo, PropertyTypeInfo propertyTypeInfo, boolean required) {
        this.key = key;
        this.propertyValueInfo = propertyValueInfo;
        this.propertyTypeInfo = propertyTypeInfo;
        isRequired = required;
    }

    public String getKey() {
        return key;
    }

    public PropertyTypeInfo getPropertyTypeInfo() {
        return propertyTypeInfo;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public PropertyValueInfo getPropertyValueInfo() {
        return propertyValueInfo;
    }
}
