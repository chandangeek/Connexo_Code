package com.energyict.mdc.rest.impl.properties;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:40
 */
@XmlRootElement
public class PropertyValueInfo<T> {

    public T inheritedValue;
    public T defaultValue;
    public T value;

    /**
     * Default constructor 4 JSON deserialization
     */
    public PropertyValueInfo() {
    }

    public PropertyValueInfo(T value, T inheritedValue, T defaultValue) {
        this(inheritedValue, defaultValue);
        this.value = value;
    }

    public PropertyValueInfo(T inheritedValue, T defaultValue) {
        this.inheritedValue = inheritedValue;
        this.defaultValue = defaultValue;
    }

    public T getInheritedValue() {
        return inheritedValue;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }
}
