package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertyValueConverter;
import com.elster.jupiter.properties.SimplePropertyType;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.util.Checks;

/**
 * Created by mbarinov on 17.08.2016.
 */
public class DefaultPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return true; // it can process any property spec
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.getTypeFrom(propertySpec.getValueFactory());
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue != null && infoValue instanceof String && !Checks.is((String) infoValue)
                .emptyOrOnlyWhiteSpace()) {
            return propertySpec.getValueFactory().fromStringValue((String) infoValue);
        }
        return null;
    }

    @Override
    public PropertyValueInfo convertValueToInfo(PropertySpec propertySpec, Object propertyValue, Object defaultValue) {
        return new PropertyValueInfo<>(propertyValue, defaultValue);
    }

}
