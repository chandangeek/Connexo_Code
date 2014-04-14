package com.energyict.mdc.pluggable.rest;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides value information regarding a property.
 * All 'values' can be present or absent at the same time.
 * The first not null value should be displayed to the user in the following order:
 * <ol>
 *     <li>Value</li>
 *     <li>InheritedValue</li>
 *     <li>DefaultValue</li>
 * </ol>
 *
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

    private PropertyValueInfo(T inheritedValue, T defaultValue) {
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
