package com.elster.jupiter.rest.util.properties;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The <i>ROOT</i> Property object.
 * This contains a:
 * <ul>
 *     <li>Key: the name of the property</li>
 *     <li>PropertyValueInfo: the value (userDefined, inherited or default) for this property</li>
 *     <li>PropertyTypeInfo: additional information regarding the type of this property</li>
 *     <li>Required: indicating whether or not this is a required property</li>
 * </ul>
 *
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:37
 */
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
