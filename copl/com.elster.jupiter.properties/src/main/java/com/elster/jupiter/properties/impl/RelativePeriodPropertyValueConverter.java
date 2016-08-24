package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertyValueConverter;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.SimplePropertyType;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;

import java.util.HashMap;
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
    public PropertyValueInfo convertValueToInfo(PropertySpec propertySpec, Object propertyValue, Object defaultValue) {
        Map<String, Integer> defaultValueMap = new HashMap<>();
        if (defaultValue != null) {
            defaultValueMap.put("id", 0);
            if (propertyValue != null && propertyValue.toString().equals(defaultValue.toString())) {
                propertyValue = defaultValueMap;
            }
        }
        return new PropertyValueInfo<>(propertyValue, defaultValueMap);
    }

}
