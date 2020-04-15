package com.elster.jupiter.systemproperties;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyType;

public interface SystemPropertySpec {

    String getKey();
    String getDescription();
    PropertyType getPropertyType();
    void actionOnChange(SystemProperty property);
    String getDefaultValue();
    PropertyInfo preparePropertyInfo(SystemProperty property);
    String convertValueToString(PropertyInfo propertyInfo);
}
