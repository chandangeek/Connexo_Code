/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PropertyInfo {

    public String key;
    public String name;
    public PropertyValueInfo<?> propertyValueInfo;
    public PropertyTypeInfo propertyTypeInfo;
    public boolean required;

    /**
     * Default constructor 4 JSON deserialization
     */
    public PropertyInfo() {
    }

    public PropertyInfo(String name, String translationKey, PropertyValueInfo<?> propertyValueInfo, PropertyTypeInfo propertyTypeInfo, boolean required) {
        this.key = translationKey;
        this.name = name;
        this.propertyValueInfo = propertyValueInfo;
        this.propertyTypeInfo = propertyTypeInfo;
        this.required = required;
    }

    public PropertyValueInfo<?> getPropertyValueInfo() {
        return propertyValueInfo;
    }
}
