package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error;

public class SCIMException extends RuntimeException {

    private final SCIMError scimError;

    public SCIMException(final SCIMError scimError) {
        this.scimError = scimError;
    }

    public SCIMError getScimError() {
        return scimError;
    }

}
