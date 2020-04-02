package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.http.whiteboard.TokenValidation;
import com.elster.jupiter.rest.util.MimeTypesExt;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.osgi.service.http.HttpContext;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * Performs client credentials flow authentication and jwt token authorization according
 * to the OAuth 2.0 protocol. Check RFC for more details.
 */
public class OAuth2Authorization implements HttpContext {

    private InboundEndPointConfiguration endPointConfiguration;
    private TokenService tokenService;

    @Inject
    public OAuth2Authorization(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public OAuth2Authorization init(InboundEndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return false;
        }

        if (authorizationHeader.startsWith("Basic ")) {
            final byte[] clientCredentials = Base64.getDecoder().decode(authorizationHeader.substring(6).getBytes());
            return validateClientCredentials(clientCredentials);
        } else if (authorizationHeader.startsWith("Bearer ")) {

            TokenValidation tokenValidation = null;
            try {
                tokenValidation = tokenService.validateServiceSignedJWT(SignedJWT.parse(authorizationHeader.substring(7)));
            } catch (JOSEException | ParseException e) {
                e.printStackTrace();
            }

            return Objects.requireNonNull(tokenValidation).isValid();
        }

        return true;
    }

    @Override
    public URL getResource(String name) {
        return InboundRestEndPoint.class.getResource(name);
    }

    @Override
    public String getMimeType(String name) {
        return MimeTypesExt.get().getByFile(name);
    }

    private boolean validateClientCredentials(final byte[] clientCredentials) {
        final byte[] endPointclientCredentials = (endPointConfiguration.getClientId() + ":" + endPointConfiguration.getClientSecret()).getBytes();
        return Arrays.equals(endPointclientCredentials, clientCredentials);
    }
}
