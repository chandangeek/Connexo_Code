package com.energyict.mdc.device.data.rest.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class EstimatorPropertiesExceptionMapper implements ExceptionMapper<EstimatorPropertiesException> {
    @Override
    public Response toResponse(EstimatorPropertiesException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(EstimatorPropertiesExceptionInfo.from(exception)).build();
    }
}
