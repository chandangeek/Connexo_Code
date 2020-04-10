package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error;

public enum SCIMError {

    NOT_FOUND("404", "Entity not found", 404);

    String errorCode;

    String description;

    int httpCode;

    SCIMError(String errorCode, String description, int httpCode) {
        this.errorCode = errorCode;
        this.description = description;
        this.httpCode = httpCode;
    }
}
