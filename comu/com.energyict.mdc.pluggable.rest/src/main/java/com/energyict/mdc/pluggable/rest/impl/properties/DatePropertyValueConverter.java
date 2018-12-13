/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.energyict.mdc.dynamic.DateAndTimeFactory;

import java.util.Date;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class DatePropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) { return propertySpec != null && !(propertySpec.getValueFactory() instanceof DateAndTimeFactory)
            && Date.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()); }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.DATE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return new Date((long) infoValue);
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return domainValue;
    }

}
