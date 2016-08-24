package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertyValueConverter;
import com.elster.jupiter.properties.SimplePropertyType;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;

import java.time.Instant;

/**
 * Created by mbarinov on 17.08.2016.
 */
public class InstantPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory().getValueType().equals(Instant.class);
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.TIMESTAMP;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue != null && (infoValue instanceof Long)) {
            return Instant.ofEpochMilli((Long) infoValue);
        }
        return null;
    }

    @Override
    public PropertyValueInfo convertValueToInfo(PropertySpec propertySpec, Object propertyValue, Object defaultValue) {
        return new PropertyValueInfo<>(propertyValue, defaultValue);
    }

}
