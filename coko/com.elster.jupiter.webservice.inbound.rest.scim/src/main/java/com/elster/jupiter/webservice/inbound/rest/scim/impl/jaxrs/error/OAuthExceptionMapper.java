package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class OAuthExceptionMapper implements ExceptionMapper<OAuthException> {

    @Override
    public Response toResponse(OAuthException exception) {
        return Response.status(exception.getOAuthError().httpCode)
                .entity(createResponseDTOFromException(exception))
                .build();
    }

    private ErrorResponse createResponseDTOFromException(final OAuthException exception) {
        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError(exception.getOAuthError().errorCode);
        errorResponse.setErrorDescription(exception.getOAuthError().description);
        return errorResponse;
    }
}
