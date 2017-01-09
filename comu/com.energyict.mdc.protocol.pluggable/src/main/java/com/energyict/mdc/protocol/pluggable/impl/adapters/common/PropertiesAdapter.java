package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.TypedProperties;

/**
 * Serves as an Adapter for property related functionality
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/01/13
 * Time: 14:27
 */
public class PropertiesAdapter {

    private final TypedProperties properties = TypedProperties.empty();

    public void copyProperties (com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        /* First we add the inherited properties, then the others (so we can overwrite the inherited */
        if (typedProperties.getInheritedProperties() != null) {
            properties.setAllProperties(typedProperties.getInheritedProperties());
        }
        properties.setAllProperties(typedProperties);
    }

    public void setProperty(String value, Object object) {
        this.properties.setProperty(value, object);
    }

    public TypedProperties getProperties() {
        return properties;
    }
}