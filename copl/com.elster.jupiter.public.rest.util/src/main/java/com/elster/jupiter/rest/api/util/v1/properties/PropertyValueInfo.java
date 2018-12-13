/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PropertyValueInfo<T> {
    public T inheritedValue;
    public T defaultValue;
    public T value;
    public Boolean propertyHasValue;

    /**
     * Default constructor 4 JSON deserialization
     */
    public PropertyValueInfo() {
    }

    public PropertyValueInfo(T value, T inheritedValue, T defaultValue, Boolean propertyHasValue) {
        this.value = value;
        this.inheritedValue = inheritedValue;
        this.defaultValue = defaultValue;
        this.propertyHasValue = propertyHasValue;
    }

    public PropertyValueInfo(boolean propertyHasValue) {
        this.propertyHasValue = propertyHasValue;
    }

    public PropertyValueInfo(T value, T defaultValue, Boolean propertyHasValue) {
        this(value, null, defaultValue, propertyHasValue);
    }

    public PropertyValueInfo(T value, T defaultValue) {
        this(value, null, defaultValue, false);
    }

    public T getValue() {
        return value;
    }
}
