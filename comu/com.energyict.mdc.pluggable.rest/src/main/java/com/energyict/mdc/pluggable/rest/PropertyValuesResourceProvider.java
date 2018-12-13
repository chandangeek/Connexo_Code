/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ConsumerType;

import java.net.URI;
import java.util.Optional;

/**
 * This provider defines the REST resource where possible values for a Property can be obtained.
 * Type ahead filtering is supported on this resource, by using the standard Extjs JSON query parameter 'filter'
 */
@ConsumerType
public interface PropertyValuesResourceProvider {
    /**
     * The URI on which the possible values can be retrieved by using HTTP GET. A query parameter 'filter' can be
     * specified to narrow down search results. The possible properties within the filter are context specific.
     * @see {https://confluence.eict.vpdc/pages/viewpage.action?spaceKey=JDG&title=Filter}
     * @param propertySpec The propertySpec for which the URI needs to be calculated
     * @return The complete URI on which possible values can be retrieved
     */
    Optional<URI> getPropertiesValuesResource(PropertySpec propertySpec);
}
