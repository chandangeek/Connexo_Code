/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.NonOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;

import java.util.SimpleTimeZone;

/**
 * Created by dantonov on 29.03.2017.
 */
public class NonOrBigDecimalValueConverter implements PropertyValueConverter {
    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof NonOrBigDecimalValueFactory;
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.NON_OR_BIG_DECIMAL;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        // FIXME
        if (infoValue instanceof NonOrBigDecimalValueProperty){
            return infoValue;
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if(domainValue!=null){
            if (domainValue instanceof NonOrBigDecimalValueProperty){
                return domainValue;
            }
        }
        return null;
    }
}
