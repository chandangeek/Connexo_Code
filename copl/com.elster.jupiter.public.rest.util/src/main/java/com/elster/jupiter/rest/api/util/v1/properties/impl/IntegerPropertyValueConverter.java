/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.properties.NumberValidationRules;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyType;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValidationRule;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValueConverter;
import com.elster.jupiter.rest.api.util.v1.properties.SimplePropertyType;

public class IntegerPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory().getValueType().equals(Integer.class);
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.INTEGER;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return infoValue;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return domainValue;
    }

    @Override
    public PropertyValidationRule getDefaultPropertyValidationRule() {
        NumberValidationRules<Integer> rules = new NumberValidationRules<>();
        rules.setMinimumValue(Integer.MIN_VALUE);
        rules.setMaximumValue(Integer.MAX_VALUE);
        return rules;
    }
}
