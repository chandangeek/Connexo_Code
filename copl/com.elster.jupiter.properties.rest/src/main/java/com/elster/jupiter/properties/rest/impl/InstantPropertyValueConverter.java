/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;

import java.time.Instant;

/**
 * Created by mbarinov on 17.08.2016.
 */
public class InstantPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory().getValueType().equals(Instant.class);
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.TIMESTAMP;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue != null && (infoValue instanceof Long)) {
            return Instant.ofEpochMilli((Long) infoValue);
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return domainValue;
    }

}
