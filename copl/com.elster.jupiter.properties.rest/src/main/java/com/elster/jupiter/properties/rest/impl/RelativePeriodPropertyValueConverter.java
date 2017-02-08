/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import java.util.Map;

/**
 * Created by mbarinov on 23.08.2016.
 */
public class RelativePeriodPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof RelativePeriodFactory;
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.RELATIVEPERIOD;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return propertySpec.getValueFactory().fromStringValue("" + ((Map) infoValue).get("id"));
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return RelativePeriodInfo.from((RelativePeriod) domainValue);
    }

}