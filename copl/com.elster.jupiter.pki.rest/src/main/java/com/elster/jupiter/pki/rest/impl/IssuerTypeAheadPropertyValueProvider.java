package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.PropertyValuesResourceProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

public class IssuerTypeAheadPropertyValueProvider implements PropertyValuesResourceProvider {
    private final UriInfo uriInfo;

    public IssuerTypeAheadPropertyValueProvider(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Optional<URI> getPropertiesValuesResource(PropertySpec propertySpec) {
        if (propertySpec.getName().equals("issuer")) {
            return Optional.of(uriInfo.getBaseUriBuilder()
                    .path(CertificateWrapperResource.class, "issuerSource")
                    .build());
        } else {
            return Optional.empty();
        }
    }
}
