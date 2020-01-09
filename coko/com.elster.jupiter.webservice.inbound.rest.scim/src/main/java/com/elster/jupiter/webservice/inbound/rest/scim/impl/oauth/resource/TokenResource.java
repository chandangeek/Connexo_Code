package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.TokenService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("token")
public class TokenResource {

    @Inject
    private TokenService tokenService;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public TokenResponse getToken(@FormParam("grant_type") String grantType) {
        return tokenService.createTokenResponse(null);
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response verifyToken(String jws) {
        return Response.ok().entity(tokenService.verifyToken(jws)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TokenResponse getTokenGet() {
        return tokenService.createTokenResponse(null);
    }

}
