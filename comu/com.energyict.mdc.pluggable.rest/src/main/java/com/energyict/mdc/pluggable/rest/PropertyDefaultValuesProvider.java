/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;

import java.util.List;

public interface PropertyDefaultValuesProvider {
    List<?> getPropertyPossibleValues(PropertySpec propertySpec, PropertyType propertyType);
}
