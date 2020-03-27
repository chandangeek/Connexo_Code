package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthError;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthException;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;

@Path("token")
public class TokenResource {

    @Inject
    private TokenService tokenService;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public TokenResponse getToken(@FormParam("grant_type") String grantType) throws JOSEException, ParseException {

        if (grantType == null) {
            throw new OAuthException(OAuthError.INVALID_REQUEST);
        }

        if (!grantType.equalsIgnoreCase("client_credentials")) {
            throw new OAuthException(OAuthError.UNSUPPORTED_GRANT_TYPE);
        }

        final long expiresIn = getExpirationDate().toEpochMilli();

        final SignedJWT serviceSignetJWT = tokenService.createServiceSignedJWT(expiresIn, "client", "connexo", new HashMap<>());

        return TokenResponse.TokenResponseBuilder.aTokenResponse()
                .withAccessToken(serviceSignetJWT.serialize())
                .withTokenType("bearer")
                .withExpiresIn(expiresIn)
                .build();
    }

    private Instant getExpirationDate() {
        final long currentTimeMillis = System.currentTimeMillis();
        final long timeToLive = 30 * 60 * 1000;
        final long expirationTimeMillis = currentTimeMillis + timeToLive;
        return Instant.ofEpochMilli(expirationTimeMillis);
    }

}
