package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.OAuthErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class OAuthExceptionMapper implements ExceptionMapper<OAuthException> {

    @Override
    public Response toResponse(OAuthException exception) {
        final OAuthErrorResponse oAuthErrorResponse = new OAuthErrorResponse();
        oAuthErrorResponse.setError(exception.getOAuthError().errorCode);
        oAuthErrorResponse.setErrorDescription(exception.getOAuthError().description);
        return Response.status(exception.getOAuthError().httpCode).entity(oAuthErrorResponse).build();
    }
}
