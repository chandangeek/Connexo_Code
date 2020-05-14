package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error;

public class OAuthException extends RuntimeException {

    private final OAuthError oAuthError;

    public OAuthException(final OAuthError oAuthError) {
        this.oAuthError = oAuthError;
    }

    public OAuthError getOAuthError() {
        return oAuthError;
    }
}
