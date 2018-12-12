/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.pluggable.rest.PropertyValuesResourceProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

/**
 * Provides access to AlIAS type-ahead resource
 */
public class AliasTypeAheadPropertyValueProvider implements PropertyValuesResourceProvider {
    private final UriInfo uriInfo;

    public AliasTypeAheadPropertyValueProvider(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Optional<URI> getPropertiesValuesResource(PropertySpec propertySpec) {
        if (propertySpec.getName().equals("alias")) {
            return Optional.of(uriInfo.getBaseUriBuilder()
                    .path(SecurityAccessorTypeResource.class)
                    .path(SecurityAccessorTypeResource.class, "aliasSource")
                    .build());
        } else {
            return Optional.empty();
        }
    }
}
