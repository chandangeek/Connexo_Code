package com.energyict.mdc.rest.impl.properties;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Provides additional type information of a specific property.
 *
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:40
 */
@XmlRootElement
public class PropertyTypeInfo {

    /**
     * Identifies the backEnd type of the property
     */
    public SimplePropertyType simplePropertyType;
    /**
     * Provides validation rules for this property (min/max/...)
     */
    public PropertyValidationRule propertyValidationRule;
    /**
     * Defines predefined values which the user should be able to choose from
     */
    public PredefinedPropertyValuesInfo predefinedPropertyValuesInfo;
    /**
     * Provides a URI where a reference object-list can be fetched
     */
    public URI referenceUri;

    /**
     * Default constructor 4 JSON deserialization
     */
    public PropertyTypeInfo() {
    }

    public PropertyTypeInfo(SimplePropertyType simplePropertyType, PropertyValidationRule propertyValidationRule, PredefinedPropertyValuesInfo predefinedPropertyValuesInfo, URI referenceUri) {
        this.simplePropertyType = simplePropertyType;
        this.propertyValidationRule = propertyValidationRule;
        this.predefinedPropertyValuesInfo = predefinedPropertyValuesInfo;
        this.referenceUri = referenceUri;
    }

    public SimplePropertyType getSimplePropertyType() {
        return simplePropertyType;
    }

    public PropertyValidationRule getPropertyValidationRule() {
        return propertyValidationRule;
    }

    public PredefinedPropertyValuesInfo getPredefinedPropertyValuesInfo() {
        return predefinedPropertyValuesInfo;
    }

    public URI getReferenceUri() {
        return referenceUri;
    }
}
