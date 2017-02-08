/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.rest.api.util.v1.RelativePeriodInfo;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValueConverter;
import com.elster.jupiter.rest.api.util.v1.properties.SimplePropertyType;
import com.elster.jupiter.time.RelativePeriod;

import java.util.Map;

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
