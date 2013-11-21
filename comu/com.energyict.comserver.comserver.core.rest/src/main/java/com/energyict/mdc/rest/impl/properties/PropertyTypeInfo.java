package com.energyict.mdc.rest.impl.properties;

import java.net.URI;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:40
 */
public class PropertyTypeInfo {

    final SimplePropertyType simplePropertyType;
    final PropertyValidationRule propertyValidationRule;
    final PredefinedPropertyValuesInfo predefinedPropertyValuesInfo;
    final URI referenceUri;

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
