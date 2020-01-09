package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthError;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.Base64;

@Provider
@OAuthOnlyFilter
public class BasicAuthorizationFilter implements ContainerRequestFilter {

    protected static final byte[] CLIENT_CREDENTIALS = Base64.getEncoder().encode("enexis.password".getBytes());

    @Override
    public void filter(ContainerRequestContext requestContext) {
        final String authorization = requestContext.getHeaders().getFirst("Authorization");

        if (authorization == null) {
            throw new OAuthException(OAuthError.INVALID_CLIENT);
        }

        final String[] partedAuthorization = authorization.split("\\s+");

        if (partedAuthorization.length != 2) {
            throw new OAuthException(OAuthError.INVALID_CLIENT);
        }

        if (!partedAuthorization[0].equalsIgnoreCase("Basic")) {
            throw new OAuthException(OAuthError.INVALID_CLIENT);
        }

        if (!Arrays.equals(CLIENT_CREDENTIALS, partedAuthorization[1].getBytes())) {
            throw new OAuthException(OAuthError.INVALID_GRANT);
        }
    }

}
