/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.properties.PropertySpec;

import java.net.URI;
import java.util.Optional;

/**
 * This provider defines the REST resource where possible values for a Property can be obtained.
 * Type ahead filtering is supported on this resource, by using the query parameter 'searchField'
 */
public interface PropertyValuesResourceProvider {
    /**
     * The URI on which the possible values can be retrieved by using HTTP GET. A query parameter 'searchField' can be
     * specified to narrow down search results.
     * @see {https://confluence.eict.vpdc/pages/viewpage.action?spaceKey=JDG&title=Filter}
     * @param propertySpec The propertySpec for which the URI needs to be calculated
     * @return The complete URI on which possible values can be retrieved
     */
    Optional<URI> getPropertiesValuesResource(PropertySpec propertySpec);
}
