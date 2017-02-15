/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;

import java.util.Map;

/**
 * Extension of HasDynamicPropertiesWithValues to allow for setting of properties.
 */
public interface HasDynamicPropertiesWithUpdatableValues extends HasDynamicPropertiesWithValues {


    void setProperties(Map<String, Object> properties);
}
