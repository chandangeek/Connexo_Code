package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


public class LocalizedExceptionMapper implements ExceptionMapper<LocalizedException>{

    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public LocalizedExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(LocalizedException exception) {
        ConstraintViolationInfo constraintViolationInfo = infoProvider.get();
        constraintViolationInfo.from(exception);

        int status = StatusCode.UNPROCESSABLE_ENTITY.getStatusCode();
        if (exception instanceof ExceptionFactory.RestException) {
            status = ((ExceptionFactory.RestException) exception).getStatus().getStatusCode();
        }
        return Response.status(status).entity(constraintViolationInfo).build();
    }

}
