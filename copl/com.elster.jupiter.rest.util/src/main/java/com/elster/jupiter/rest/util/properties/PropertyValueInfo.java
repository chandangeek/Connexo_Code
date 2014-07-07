package com.elster.jupiter.rest.util.properties;

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
        this.value = value;
        this.inheritedValue = inheritedValue;
        this.defaultValue = defaultValue;
    }
    
    public PropertyValueInfo(T value, T defaultValue) {
        this(value, null, defaultValue);
    }

    public T getValue() {
        return value;
    }
}
