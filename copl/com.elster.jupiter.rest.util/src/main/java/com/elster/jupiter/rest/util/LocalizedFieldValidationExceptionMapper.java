package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class LocalizedFieldValidationExceptionMapper implements ExceptionMapper<LocalizedFieldValidationException>{

    private final NlsService nlsService;

    @Inject
    public LocalizedFieldValidationExceptionMapper(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Override
    public Response toResponse(LocalizedFieldValidationException fieldException) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ConstraintViolationInfo(nlsService).from(fieldException)).build();
    }

}
