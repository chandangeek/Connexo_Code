package com.energyict.mdc.device.data.rest.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class EstimationErrorExceptionMapper implements ExceptionMapper<EstimationErrorException> {

    @Override
    public Response toResponse(EstimationErrorException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(EstimationErrorInfo.from(exception)).build();
    }
}
