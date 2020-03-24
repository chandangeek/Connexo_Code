/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.NoneOrBigDecimal;
import com.elster.jupiter.properties.NoneOrBigDecimalValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;

import java.math.BigDecimal;
import java.util.Map;

public class NoneOrBigDecimalValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof NoneOrBigDecimalValueFactory;
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.NONE_OR_BIGDECIMAL;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue instanceof Map<?, ?>) {
            Boolean isNone = (Boolean) ((Map) infoValue).get("isNone");
            if (isNone) {
                return NoneOrBigDecimal.none();
            } else {
                Object valueObj = ((Map) infoValue).get("value");
                if (valueObj instanceof String || valueObj instanceof Integer || valueObj instanceof Double) {
                    return NoneOrBigDecimal.of(new BigDecimal(valueObj.toString()));
                }
            }
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if (domainValue != null) {
            if (domainValue instanceof NoneOrBigDecimal) {
                return String.valueOf(domainValue);
            }
        }
        return null;
    }
}
