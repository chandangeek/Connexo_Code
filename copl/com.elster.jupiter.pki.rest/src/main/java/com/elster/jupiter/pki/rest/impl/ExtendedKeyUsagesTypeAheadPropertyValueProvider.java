package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.PropertyValuesResourceProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

public class ExtendedKeyUsagesTypeAheadPropertyValueProvider implements PropertyValuesResourceProvider {
    private final UriInfo uriInfo;

    public ExtendedKeyUsagesTypeAheadPropertyValueProvider(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Optional<URI> getPropertiesValuesResource(PropertySpec propertySpec) {
        if (propertySpec.getName().equals("extendedkeyusages")) {
            return Optional.of(uriInfo.getBaseUriBuilder()
                    .path(CertificateWrapperResource.class, "extendedKeyUsagesSource")
                    .build());
        } else {
            return Optional.empty();
        }
    }
}
