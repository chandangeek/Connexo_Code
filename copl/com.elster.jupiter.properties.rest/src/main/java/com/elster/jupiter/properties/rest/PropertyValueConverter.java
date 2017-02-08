/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;

import com.elster.jupiter.properties.PropertySpec;

/**
 * Created by mbarinov on 17.08.2016.
 */
public interface PropertyValueConverter {

    boolean canProcess(PropertySpec propertySpec);

    PropertyType getPropertyType(PropertySpec propertySpec);

    Object convertInfoToValue(PropertySpec propertySpec, Object infoValue);

    Object convertValueToInfo(PropertySpec propertySpec, Object domainValue);
}
