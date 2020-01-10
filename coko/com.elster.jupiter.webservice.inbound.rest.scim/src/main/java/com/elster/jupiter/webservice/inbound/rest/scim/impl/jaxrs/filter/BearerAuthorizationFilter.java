package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthError;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthException;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.TokenService;
import io.jsonwebtoken.Jwt;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@SCIMResourceOnlyFilter
public class BearerAuthorizationFilter implements ContainerRequestFilter {

    @Inject
    private TokenService tokenService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String authorizationHeader = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null) {
            throw new OAuthException(OAuthError.INVALID_CLIENT);
        }

        final String[] partedAuthorization = authorizationHeader.split("\\s+");

        if (partedAuthorization.length != 2) {
            throw new OAuthException(OAuthError.INVALID_CLIENT);
        }

        if (!partedAuthorization[0].equalsIgnoreCase("Bearer")) {
            throw new OAuthException(OAuthError.INVALID_CLIENT);
        }

        final Jwt<?, ?> jwt = tokenService.verifyToken(partedAuthorization[1]);
    }
}
