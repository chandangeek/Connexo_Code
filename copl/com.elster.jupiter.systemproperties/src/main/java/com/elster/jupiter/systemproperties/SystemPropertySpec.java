package com.elster.jupiter.systemproperties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SystemPropertySpec {

    String getKey();
    void actionOnChange(SystemProperty property);
    PropertyInfo preparePropertyInfo(SystemProperty property);
    String convertValueToString(PropertyInfo propertyInfo);
}
