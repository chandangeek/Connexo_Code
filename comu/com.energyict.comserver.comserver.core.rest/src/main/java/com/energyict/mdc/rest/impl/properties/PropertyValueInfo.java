package com.energyict.mdc.rest.impl.properties;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:40
 */
public class PropertyValueInfo<T> {

    final Object inheritedValue;
    final Object defaultValue;
    Object value;

    public PropertyValueInfo(T value, T inheritedValue, T defaultValue) {
        this(inheritedValue, defaultValue);
        this.value = value;
    }

    public PropertyValueInfo(T inheritedValue, T defaultValue) {
        this.inheritedValue = inheritedValue;
        this.defaultValue = defaultValue;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Object getInheritedValue() {
        return inheritedValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

}
