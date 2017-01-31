/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.TypedProperties;

public class PropertiesAdapter {

    private final TypedProperties properties = TypedProperties.empty();

    public void copyProperties (TypedProperties typedProperties) {
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
