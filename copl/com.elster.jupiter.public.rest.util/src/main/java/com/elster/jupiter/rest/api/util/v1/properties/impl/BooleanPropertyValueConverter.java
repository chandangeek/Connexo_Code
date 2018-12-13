/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties.impl;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyType;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValueConverter;
import com.elster.jupiter.rest.api.util.v1.properties.SimplePropertyType;

public class BooleanPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof BooleanFactory;
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.BOOLEAN;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue != null && infoValue instanceof Boolean) {
            return infoValue;
        }
        return Boolean.FALSE;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return domainValue;
    }

}
