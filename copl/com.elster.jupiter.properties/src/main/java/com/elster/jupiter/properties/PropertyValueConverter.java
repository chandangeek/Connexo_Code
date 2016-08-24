package com.elster.jupiter.properties;

import com.elster.jupiter.rest.util.properties.PropertyType;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;

/**
 * Created by mbarinov on 17.08.2016.
 */
public interface PropertyValueConverter {

    boolean canProcess(PropertySpec propertySpec);

    PropertyType getPropertyType(PropertySpec propertySpec);

    Object convertInfoToValue(PropertySpec propertySpec, Object infoValue);

    PropertyValueInfo convertValueToInfo(PropertySpec propertySpec, Object propertyValue, Object defaultValue);
}
