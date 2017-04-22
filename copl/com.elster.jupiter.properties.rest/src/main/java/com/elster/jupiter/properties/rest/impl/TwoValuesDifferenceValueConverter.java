/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;

import java.math.BigDecimal;
import java.util.Map;

public class TwoValuesDifferenceValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof TwoValuesDifferenceValueFactory;
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.TWO_VALUES_DIFFERENCE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue instanceof Map<?, ?>) {
            String type = (String) ((Map) infoValue).get("type");
            Object valueObj = ((Map) infoValue).get("value");
            Double value = null;
            if (valueObj instanceof Integer) {
                value = ((Integer) valueObj).doubleValue();
            } else if (valueObj instanceof Double) {
                value = (Double) valueObj;
            }
            if (value != null) {
                return new TwoValuesDifference(TwoValuesDifference.Type.fromString(type), BigDecimal.valueOf(value));
            }
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if (domainValue != null) {
            if (domainValue instanceof TwoValuesDifference) {
                return domainValue;
            }
        }
        return null;
    }
}
