/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.units.Quantity;

import java.util.Map;

/**
 * Created by mbarinov on 19.08.2016.
 */
public class QuantityPropertyValueConverter implements PropertyValueConverter {

    static class QuantityInfo {
        public String id;
        public String displayValue;
    }

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof QuantityValueFactory;
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.QUANTITY;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue != null && infoValue instanceof Map) {
            Map value = (Map) infoValue;
            if (value.get("id") != null) {
                return new QuantityValueFactory().fromStringValue(value.get("id").toString());
            }
        } else if (infoValue instanceof String) {
            return new QuantityValueFactory().fromStringValue((String) infoValue);
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        QuantityInfo quantityInfo = new QuantityInfo();
        if (domainValue != null && domainValue instanceof Quantity) {
            Quantity value = (Quantity) domainValue;
            quantityInfo.id = new QuantityValueFactory().toStringValue(value);
            String[] valueParts = value.toString().split(" ");
            quantityInfo.displayValue = valueParts[1];
        }
        return quantityInfo;
    }
}
