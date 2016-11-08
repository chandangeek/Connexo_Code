package com.elster.jupiter.properties.rest;

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
