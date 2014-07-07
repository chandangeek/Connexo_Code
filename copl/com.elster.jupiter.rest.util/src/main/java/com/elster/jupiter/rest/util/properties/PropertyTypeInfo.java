package com.elster.jupiter.rest.util.properties;

import java.net.URI;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
    @XmlJavaTypeAdapter(PropertyTypeAdapter.class)
    public PropertyType simplePropertyType;
    /**
     * Provides validation rules for this property (min/max/...)
     */
    @XmlJavaTypeAdapter(PropertyValidationRuleAdapter.class)
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
