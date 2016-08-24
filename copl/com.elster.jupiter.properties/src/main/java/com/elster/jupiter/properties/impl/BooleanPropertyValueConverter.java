package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertyValueConverter;
import com.elster.jupiter.properties.SimplePropertyType;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;

/**
 * Created by mbarinov on 17.08.2016.
 */
public class BooleanPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof BooleanFactory;
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.BOOLEAN;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue != null && infoValue instanceof Boolean) {
            return infoValue;
        }
        return Boolean.FALSE;
    }

    @Override
    public PropertyValueInfo convertValueToInfo(PropertySpec propertySpec, Object propertyValue, Object defaultValue) {
        return new PropertyValueInfo<>(propertyValue, defaultValue);
    }

}
