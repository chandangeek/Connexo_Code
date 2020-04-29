package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class SCIMExceptionMapper implements ExceptionMapper<SCIMException> {

    @Override
    public Response toResponse(final SCIMException exception) {
        return Response.status(exception.getScimError().httpCode)
                .entity(createResponseDTOFromException(exception))
                .build();
    }

    private ErrorResponse createResponseDTOFromException(final SCIMException exception) {
        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError(exception.getScimError().errorCode);
        errorResponse.setErrorDescription(exception.getScimError().description);
        return errorResponse;
    }
}
