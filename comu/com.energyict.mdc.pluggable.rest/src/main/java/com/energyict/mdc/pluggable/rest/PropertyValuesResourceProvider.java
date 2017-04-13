/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.properties.PropertySpec;

import java.net.URI;

/**
 * This provider defines the REST resource where possible values for a Property can be obtained.
 * Type ahead is supported on this resource.
 */
public interface PropertyValuesResourceProvider {
    /**
     * The URI on which the possible values
     * @param propertySpec The propertySpec for which the URI needs to be calculated
     * @return The complete URI on which possible values can be retrieved
     */
    URI getPropertiesValuesResource(PropertySpec propertySpec);
}
