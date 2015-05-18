package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;

import java.util.List;

public interface PropertyDefaultValuesProvider {
    List<?> getPropertyPossibleValues(PropertySpec propertySpec, PropertyType propertyType);
}
