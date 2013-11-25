package com.energyict.mdc.rest.impl.properties;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:40
 */
@XmlRootElement
public class PropertyTypeInfo {

    public SimplePropertyType simplePropertyType;
    public PropertyValidationRule propertyValidationRule;
    public PredefinedPropertyValuesInfo predefinedPropertyValuesInfo;
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
