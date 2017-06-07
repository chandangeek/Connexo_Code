/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties;

import com.elster.jupiter.properties.PropertySpec;

public interface PropertyValueConverter {

    boolean canProcess(PropertySpec propertySpec);

    PropertyType getPropertyType(PropertySpec propertySpec);

    Object convertInfoToValue(PropertySpec propertySpec, Object infoValue);

    Object convertValueToInfo(PropertySpec propertySpec, Object domainValue);

    default PropertyValidationRule getDefaultPropertyValidationRule(){
        return null;
    }
}
