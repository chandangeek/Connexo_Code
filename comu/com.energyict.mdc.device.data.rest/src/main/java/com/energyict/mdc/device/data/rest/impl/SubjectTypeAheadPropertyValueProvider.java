package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.pluggable.rest.PropertyValuesResourceProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

/**
 * Provides access to SUBJECT type-ahead resource
 */
public class SubjectTypeAheadPropertyValueProvider  implements PropertyValuesResourceProvider {
    private final UriInfo uriInfo;

    public SubjectTypeAheadPropertyValueProvider(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Optional<URI> getPropertiesValuesResource(PropertySpec propertySpec) {
        if (propertySpec.getName().equals("subject")) {
            return Optional.of(uriInfo.getBaseUriBuilder()
                    .path(DeviceResource.class)
                    .path(DeviceResource.class, "getSecurityAccessorResource")
                    .path(SecurityAccessorResource.class, "subjectSource")
                    .build("x"));
        } else {
            return Optional.empty();
        }
    }
}
