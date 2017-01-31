/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement
public class PropertyTypeInfo {

    /**
     * Identifies the backEnd type of the property
     */
    @JsonDeserialize(using = NullifyingDeserializer.class)
    public PropertyType simplePropertyType;
    /**
     * Provides validation rules for this property (min/max/...)
     */
    @JsonDeserialize(using = NullifyingDeserializer.class)
    public PropertyValidationRule propertyValidationRule;
    /**
     * Defines predefined values which the user should be able to choose from
     */
    public PredefinedPropertyValuesInfo<?> predefinedPropertyValuesInfo;
    /**
     * Provides a URI where a reference object-list can be fetched
     */
    public URI referenceUri;
    /**
     * Default constructor 4 JSON deserialization
     */
    public PropertyTypeInfo() {
    }

    public PropertyTypeInfo(PropertyType simplePropertyType, PropertyValidationRule propertyValidationRule, PredefinedPropertyValuesInfo<?> predefinedPropertyValuesInfo, URI referenceUri) {
        this.simplePropertyType = simplePropertyType;
        this.propertyValidationRule = propertyValidationRule;
        this.predefinedPropertyValuesInfo = predefinedPropertyValuesInfo;
        this.referenceUri = referenceUri;
    }

}
