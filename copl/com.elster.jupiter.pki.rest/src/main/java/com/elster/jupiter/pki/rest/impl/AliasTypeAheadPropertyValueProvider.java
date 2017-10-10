package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.PropertyValuesResourceProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

public class AliasTypeAheadPropertyValueProvider implements PropertyValuesResourceProvider {
    private final UriInfo uriInfo;

    public AliasTypeAheadPropertyValueProvider(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Optional<URI> getPropertiesValuesResource(PropertySpec propertySpec) {
        if (propertySpec.getName().equals("alias")) {
            return Optional.of(uriInfo.getBaseUriBuilder()
                    .path(CertificateWrapperResource.class, "aliasSource")
                    .build());
        } else {
            return Optional.empty();
        }
    }
}