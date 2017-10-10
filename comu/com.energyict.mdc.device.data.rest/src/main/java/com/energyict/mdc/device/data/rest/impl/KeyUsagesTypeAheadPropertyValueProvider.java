package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.pluggable.rest.PropertyValuesResourceProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

public class KeyUsagesTypeAheadPropertyValueProvider implements PropertyValuesResourceProvider {
    private final UriInfo uriInfo;

    public KeyUsagesTypeAheadPropertyValueProvider(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Optional<URI> getPropertiesValuesResource(PropertySpec propertySpec) {
        if (propertySpec.getName().equals("keyusages")) {
            return Optional.of(uriInfo.getBaseUriBuilder()
                    .path(DeviceResource.class)
                    .path(DeviceResource.class, "getSecurityAccessorResource")
                    .path(SecurityAccessorResource.class, "keyUsagesSource")
                    .build("x"));
        } else {
            return Optional.empty();
        }
    }
}
