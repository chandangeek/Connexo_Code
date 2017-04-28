/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.properties;

import com.elster.jupiter.metering.UsagePointValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;

import java.util.HashMap;
import java.util.Map;

public class UsagePointValueConverter implements PropertyValueConverter {

    public static final UsagePointValueConverter INSTANCE = new UsagePointValueConverter();

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && UsagePointValueFactory.UsagePointReference.class.isAssignableFrom(propertySpec.getValueFactory()
                .getValueType());
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return PropertyType.USAGE_POINT;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue instanceof Map) {
            Map info = (Map) infoValue;
            String id = String.valueOf(info.get("id"));
            return propertySpec.getValueFactory().fromStringValue(id.toString());
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        UsagePointValueFactory.UsagePointReference usagePointReference = (UsagePointValueFactory.UsagePointReference)domainValue;
        Map<String, String> info = new HashMap<>();
        info.put("id", String.valueOf(usagePointReference.getUsagePoint().getId()));
        info.put("name", usagePointReference.getUsagePoint().getName());
        return info;
    }
}
