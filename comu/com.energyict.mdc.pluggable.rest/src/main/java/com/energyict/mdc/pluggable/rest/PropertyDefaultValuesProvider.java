/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface PropertyDefaultValuesProvider {
    List<?> getPropertyPossibleValues(PropertySpec propertySpec, PropertyType propertyType);
}
