package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthError;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthException;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter.OAuthOnlyFilter;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.TokenService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("token")
public class TokenResource {

    @Inject
    private TokenService tokenService;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @OAuthOnlyFilter
    public TokenResponse getToken(@FormParam("grant_type") String grantType) {

        if (grantType == null) {
            throw new OAuthException(OAuthError.INVALID_REQUEST);
        }

        if (!grantType.equalsIgnoreCase("client_credentials")) {
            throw new OAuthException(OAuthError.UNSUPPORTED_GRANT_TYPE);
        }

        return tokenService.createTokenResponse();
    }
}
